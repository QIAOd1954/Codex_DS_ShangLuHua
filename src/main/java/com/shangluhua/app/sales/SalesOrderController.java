package com.shangluhua.app.sales;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sales-orders")
public class SalesOrderController {
    private final SalesOrderService salesOrderService;

    public SalesOrderController(SalesOrderService salesOrderService) {
        this.salesOrderService = salesOrderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SalesOrderResponse create(@Valid @RequestBody CreateSalesOrderRequest request) {
        return SalesOrderResponse.from(salesOrderService.create(request));
    }

    @GetMapping
    public List<SalesOrderResponse> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        return salesOrderService.search(keyword, status, startDate, endDate, page, size)
                .stream().map(SalesOrderResponse::from).toList();
    }

    @GetMapping("/{id}")
    public SalesOrderResponse get(@PathVariable Long id) {
        return SalesOrderResponse.from(salesOrderService.getOrder(id));
    }

    @PostMapping("/{id}/confirm")
    public SalesOrderResponse confirm(@PathVariable Long id) {
        return SalesOrderResponse.from(salesOrderService.confirm(id));
    }

    @PostMapping("/{id}/cancel")
    public SalesOrderResponse cancel(@PathVariable Long id) {
        return SalesOrderResponse.from(salesOrderService.cancel(id));
    }
}
