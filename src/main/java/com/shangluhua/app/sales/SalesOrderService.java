package com.shangluhua.app.sales;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shangluhua.app.common.ApiException;
import com.shangluhua.app.customer.Customer;
import com.shangluhua.app.customer.CustomerRepository;
import com.shangluhua.app.inventory.InventoryService;
import com.shangluhua.app.product.ProductSku;
import com.shangluhua.app.product.ProductSkuRepository;

@Service
public class SalesOrderService {
    private final SalesOrderRepository orderRepository;
    private final ProductSkuRepository skuRepository;
    private final CustomerRepository customerRepository;
    private final InventoryService inventoryService;

    public SalesOrderService(SalesOrderRepository orderRepository,
                             ProductSkuRepository skuRepository,
                             CustomerRepository customerRepository,
                             InventoryService inventoryService) {
        this.orderRepository = orderRepository;
        this.skuRepository = skuRepository;
        this.customerRepository = customerRepository;
        this.inventoryService = inventoryService;
    }

    @Transactional
    public SalesOrder create(CreateSalesOrderRequest request) {
        SalesOrder order = new SalesOrder();
        order.setOrderNo(newOrderNo());
        order.setWarehouseCode(request.warehouseCode() == null || request.warehouseCode().isBlank() ? "MAIN" : request.warehouseCode());
        order.setPaidAmount(defaultMoney(request.paidAmount()));
        order.setContactName(request.contactName());
        order.setContactPhone(request.contactPhone());

        if (request.customerId() != null) {
            Customer customer = customerRepository.findById(request.customerId())
                    .orElseThrow(() -> new ApiException("客户不存在: " + request.customerId()));
            order.setCustomer(customer);
        }

        BigDecimal total = BigDecimal.ZERO;
        for (CreateSalesOrderRequest.Item input : request.items()) {
            ProductSku sku = skuRepository.findById(input.skuId())
                    .orElseThrow(() -> new ApiException("SKU 不存在: " + input.skuId()));
            BigDecimal unitPrice = input.unitPrice() == null ? sku.getWholesalePrice() : input.unitPrice();
            BigDecimal amount = unitPrice.multiply(BigDecimal.valueOf(input.quantity()));

            SalesOrderItem item = new SalesOrderItem();
            item.setSku(sku);
            item.setSpuCodeSnapshot(sku.getSpu().getCode());
            item.setProductNameSnapshot(sku.getSpu().getName());
            item.setColorSnapshot(sku.getColorName());
            item.setSizeSnapshot(sku.getSizeName());
            item.setQuantity(input.quantity());
            item.setUnitPrice(unitPrice);
            item.setCostPriceSnapshot(sku.getCostPrice());
            item.setAmount(amount);
            order.addItem(item);
            total = total.add(amount);
        }

        order.setTotalAmount(total);
        order.setDebtAmount(total.subtract(order.getPaidAmount()).max(BigDecimal.ZERO));
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public List<SalesOrder> search(String keyword, String status, String startDate, String endDate) {
        if (keyword != null && !keyword.isBlank()) {
            return orderRepository.searchByKeyword(keyword);
        }
        if (status != null && !status.isBlank()) {
            return orderRepository.findByStatusOrderByCreatedAtDesc(SalesOrderStatus.valueOf(status));
        }
        if (startDate != null && endDate != null) {
            Instant start = LocalDate.parse(startDate).atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant end = LocalDate.parse(endDate).plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
            return orderRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end);
        }
        return orderRepository.findTop100ByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public SalesOrder getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException("订单不存在: " + orderId));
    }

    @Transactional
    public SalesOrder confirm(Long orderId) {
        SalesOrder order = orderRepository.lockWithItems(orderId)
                .orElseThrow(() -> new ApiException("订单不存在: " + orderId));
        if (order.getStatus() != SalesOrderStatus.DRAFT) {
            throw new ApiException("只有草稿状态的订单才能确认");
        }

        for (SalesOrderItem item : order.getItems()) {
            inventoryService.adjust(item.getSku().getId(), order.getWarehouseCode(), -item.getQuantity(), "SALES_ORDER", order.getId());
        }

        if (order.getCustomer() != null && order.getDebtAmount().compareTo(BigDecimal.ZERO) > 0) {
            Customer customer = order.getCustomer();
            customer.setDebtBalance(customer.getDebtBalance().add(order.getDebtAmount()));
            customerRepository.save(customer);
        }

        order.setStatus(SalesOrderStatus.CONFIRMED);
        order.setConfirmedAt(Instant.now());
        return orderRepository.save(order);
    }

    @Transactional
    public SalesOrder cancel(Long orderId) {
        SalesOrder order = orderRepository.lockWithItems(orderId)
                .orElseThrow(() -> new ApiException("订单不存在: " + orderId));
        if (order.getStatus() != SalesOrderStatus.CONFIRMED) {
            throw new ApiException("只有已确认的订单才能取消");
        }

        // 恢复库存
        for (SalesOrderItem item : order.getItems()) {
            inventoryService.adjust(item.getSku().getId(), order.getWarehouseCode(), item.getQuantity(), "SALES_CANCEL", order.getId());
        }

        // 扣减客户欠款
        if (order.getCustomer() != null && order.getDebtAmount().compareTo(BigDecimal.ZERO) > 0) {
            Customer customer = order.getCustomer();
            BigDecimal newDebt = customer.getDebtBalance().subtract(order.getDebtAmount()).max(BigDecimal.ZERO);
            customer.setDebtBalance(newDebt);
            customerRepository.save(customer);
        }

        order.setStatus(SalesOrderStatus.CANCELED);
        return orderRepository.save(order);
    }

    private String newOrderNo() {
        String time = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneId.systemDefault()).format(Instant.now());
        int suffix = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "SO" + time + suffix;
    }

    private BigDecimal defaultMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
