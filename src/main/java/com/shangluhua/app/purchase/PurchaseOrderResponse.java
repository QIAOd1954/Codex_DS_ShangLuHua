package com.shangluhua.app.purchase;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record PurchaseOrderResponse(
        Long id,
        String orderNo,
        String warehouseCode,
        String supplierName,
        BigDecimal totalCost,
        PurchaseOrderStatus status,
        Instant createdAt,
        Instant confirmedAt,
        List<PurchaseOrderItemResponse> items
) {
    public static PurchaseOrderResponse from(PurchaseOrder order) {
        return new PurchaseOrderResponse(
                order.getId(),
                order.getOrderNo(),
                order.getWarehouseCode(),
                order.getSupplierName(),
                order.getTotalCost(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getConfirmedAt(),
                order.getItems().stream().map(PurchaseOrderItemResponse::from).toList()
        );
    }
}
