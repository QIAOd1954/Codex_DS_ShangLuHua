package com.shangluhua.app.purchase;

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
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    public PurchaseOrderController(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PurchaseOrderResponse create(@Valid @RequestBody CreatePurchaseOrderRequest request) {
        return PurchaseOrderResponse.from(purchaseOrderService.create(request));
    }

    @GetMapping
    public List<PurchaseOrderResponse> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        return purchaseOrderService.search(keyword, status, page, size)
                .stream().map(PurchaseOrderResponse::from).toList();
    }

    @GetMapping("/{id}")
    public PurchaseOrderResponse get(@PathVariable Long id) {
        return PurchaseOrderResponse.from(purchaseOrderService.getOrder(id));
    }

    @PostMapping("/{id}/confirm")
    public PurchaseOrderResponse confirm(@PathVariable Long id) {
        return PurchaseOrderResponse.from(purchaseOrderService.confirm(id));
    }

    @PostMapping("/{id}/cancel")
    public PurchaseOrderResponse cancel(@PathVariable Long id) {
        return PurchaseOrderResponse.from(purchaseOrderService.cancel(id));
    }
}
