package com.shangluhua.app.common;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shangluhua.app.customer.CustomerRepository;
import com.shangluhua.app.inventory.InventoryBalance;
import com.shangluhua.app.inventory.InventoryBalanceRepository;
import com.shangluhua.app.product.ProductSpuRepository;
import com.shangluhua.app.sales.SalesOrder;
import com.shangluhua.app.sales.SalesOrderRepository;
import com.shangluhua.app.sales.SalesOrderStatus;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final ProductSpuRepository productRepository;
    private final CustomerRepository customerRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final InventoryBalanceRepository inventoryBalanceRepository;

    public DashboardController(ProductSpuRepository productRepository,
                               CustomerRepository customerRepository,
                               SalesOrderRepository salesOrderRepository,
                               InventoryBalanceRepository inventoryBalanceRepository) {
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.inventoryBalanceRepository = inventoryBalanceRepository;
    }

    @GetMapping
    public DashboardResponse stats() {
        long productCount = productRepository.count();
        long customerCount = customerRepository.count();

        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        Instant dayStart = today.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant dayEnd = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        List<SalesOrder> todayOrders = salesOrderRepository
                .findByCreatedAtBetweenOrderByCreatedAtDesc(dayStart, dayEnd);

        long todayOrderCount = todayOrders.size();
        BigDecimal todayRevenue = todayOrders.stream()
                .filter(o -> o.getStatus() == SalesOrderStatus.CONFIRMED)
                .map(SalesOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<InventoryBalance> lowStock = inventoryBalanceRepository.findByQuantityLessThan(10);

        return new DashboardResponse(productCount, customerCount, todayOrderCount, todayRevenue, lowStock.size());
    }

    public record DashboardResponse(
            long productCount,
            long customerCount,
            long todayOrderCount,
            BigDecimal todayRevenue,
            long lowStockCount
    ) {}
}
