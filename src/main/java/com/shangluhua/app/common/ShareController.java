package com.shangluhua.app.common;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shangluhua.app.product.ProductResponse;
import com.shangluhua.app.product.ProductService;
import com.shangluhua.app.sales.CreateSalesOrderRequest;
import com.shangluhua.app.sales.SalesOrderResponse;
import com.shangluhua.app.sales.SalesOrderService;
import org.springframework.web.bind.annotation.RequestParam;

import com.shangluhua.app.sales.SalesOrderRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/share")
public class ShareController {

    private final ProductService productService;
    private final SalesOrderService salesOrderService;
    private final SalesOrderRepository salesOrderRepository;

    public ShareController(ProductService productService, SalesOrderService salesOrderService,
                           SalesOrderRepository salesOrderRepository) {
        this.productService = productService;
        this.salesOrderService = salesOrderService;
        this.salesOrderRepository = salesOrderRepository;
    }

    @GetMapping("/products")
    public List<ProductResponse> listProducts() {
        return productService.search(null).stream().map(ProductResponse::from).toList();
    }

    @PostMapping("/orders")
    public SalesOrderResponse createOrder(@Valid @RequestBody CreateSalesOrderRequest request) {
        return SalesOrderResponse.from(salesOrderService.create(request));
    }

    @GetMapping("/orders")
    public List<SalesOrderResponse> searchOrders(@RequestParam(required = false) String phone) {
        if (phone != null && !phone.isBlank()) {
            return salesOrderRepository.findByContactPhoneOrderByCreatedAtDesc(phone)
                    .stream().map(SalesOrderResponse::from).toList();
        }
        return List.of();
    }
}
