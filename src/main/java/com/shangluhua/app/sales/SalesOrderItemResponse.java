package com.shangluhua.app.sales;

import java.math.BigDecimal;

public record SalesOrderItemResponse(
        Long id,
        Long skuId,
        String skuCode,
        String productName,
        String colorName,
        String sizeName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal amount
) {
    public static SalesOrderItemResponse from(SalesOrderItem item) {
        return new SalesOrderItemResponse(
                item.getId(),
                item.getSku().getId(),
                item.getSku().getSkuCode(),
                item.getProductNameSnapshot(),
                item.getColorSnapshot(),
                item.getSizeSnapshot(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getAmount()
        );
    }
}
