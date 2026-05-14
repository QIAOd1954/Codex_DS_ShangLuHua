package com.shangluhua.app.sales;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record SalesOrderResponse(
        Long id,
        String orderNo,
        String warehouseCode,
        Long customerId,
        String customerName,
        String contactName,
        String contactPhone,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal debtAmount,
        SalesOrderStatus status,
        Instant createdAt,
        Instant confirmedAt,
        List<SalesOrderItemResponse> items
) {
    public static SalesOrderResponse from(SalesOrder order) {
        return new SalesOrderResponse(
                order.getId(),
                order.getOrderNo(),
                order.getWarehouseCode(),
                order.getCustomer() != null ? order.getCustomer().getId() : null,
                order.getCustomer() != null ? order.getCustomer().getName() : null,
                order.getContactName(),
                order.getContactPhone(),
                order.getTotalAmount(),
                order.getPaidAmount(),
                order.getDebtAmount(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getConfirmedAt(),
                order.getItems().stream().map(SalesOrderItemResponse::from).toList()
        );
    }
}
