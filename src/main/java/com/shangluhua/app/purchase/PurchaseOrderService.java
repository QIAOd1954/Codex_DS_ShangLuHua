package com.shangluhua.app.purchase;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shangluhua.app.common.ApiException;
import com.shangluhua.app.inventory.InventoryService;
import com.shangluhua.app.product.ProductSku;
import com.shangluhua.app.product.ProductSkuRepository;

@Service
public class PurchaseOrderService {

    private final PurchaseOrderRepository orderRepository;
    private final ProductSkuRepository skuRepository;
    private final InventoryService inventoryService;

    public PurchaseOrderService(PurchaseOrderRepository orderRepository,
                                ProductSkuRepository skuRepository,
                                InventoryService inventoryService) {
        this.orderRepository = orderRepository;
        this.skuRepository = skuRepository;
        this.inventoryService = inventoryService;
    }

    @Transactional
    public PurchaseOrder create(CreatePurchaseOrderRequest request) {
        PurchaseOrder order = new PurchaseOrder();
        order.setOrderNo(newOrderNo());
        order.setWarehouseCode(request.warehouseCode() == null || request.warehouseCode().isBlank()
                ? "MAIN" : request.warehouseCode());

        Set<String> suppliers = new HashSet<>();
        BigDecimal total = BigDecimal.ZERO;

        for (CreatePurchaseOrderRequest.Item input : request.items()) {
            ProductSku sku = skuRepository.findById(input.skuId())
                    .orElseThrow(() -> new ApiException("SKU不存在: " + input.skuId()));

            BigDecimal unitCost = sku.getCostPrice();
            BigDecimal amount = unitCost.multiply(BigDecimal.valueOf(input.quantity()));

            if (sku.getSpu().getSupplierName() != null) {
                suppliers.add(sku.getSpu().getSupplierName());
            }

            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setSku(sku);
            item.setQuantity(input.quantity());
            item.setUnitCost(unitCost);
            item.setAmount(amount);
            order.addItem(item);
            total = total.add(amount);
        }

        order.setSupplierName(String.join(", ", suppliers));
        order.setTotalCost(total);
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrder> search(String keyword, String status) {
        if (keyword != null && !keyword.isBlank()) {
            return orderRepository.findByOrderNoContainingIgnoreCaseOrderByCreatedAtDesc(keyword);
        }
        if (status != null && !status.isBlank()) {
            return orderRepository.findByStatusOrderByCreatedAtDesc(PurchaseOrderStatus.valueOf(status));
        }
        return orderRepository.findTop100ByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public PurchaseOrder getOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ApiException("采购单不存在: " + id));
    }

    @Transactional
    public PurchaseOrder confirm(Long id) {
        PurchaseOrder order = orderRepository.lockWithItems(id)
                .orElseThrow(() -> new ApiException("采购单不存在: " + id));
        if (order.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw new ApiException("只有草稿状态的采购单才能确认");
        }

        for (PurchaseOrderItem item : order.getItems()) {
            inventoryService.adjust(
                    item.getSku().getId(),
                    order.getWarehouseCode(),
                    item.getQuantity(),
                    "PURCHASE_IN",
                    order.getId()
            );
        }

        order.setStatus(PurchaseOrderStatus.CONFIRMED);
        order.setConfirmedAt(Instant.now());
        return orderRepository.save(order);
    }

    @Transactional
    public PurchaseOrder cancel(Long id) {
        PurchaseOrder order = orderRepository.lockWithItems(id)
                .orElseThrow(() -> new ApiException("采购单不存在: " + id));
        if (order.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw new ApiException("只有草稿状态的采购单才能取消");
        }
        order.setStatus(PurchaseOrderStatus.CANCELED);
        return orderRepository.save(order);
    }

    private String newOrderNo() {
        String time = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                .withZone(ZoneId.systemDefault()).format(Instant.now());
        int suffix = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "PO" + time + suffix;
    }
}
