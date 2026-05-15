package com.shangluhua.app.purchase;

import java.math.BigDecimal;

public record PurchaseOrderItemResponse(
        Long id,
        Long skuId,
        String skuCode,
        String productName,
        String colorName,
        String sizeName,
        int quantity,
        BigDecimal unitCost,
        BigDecimal amount
) {
    public static PurchaseOrderItemResponse from(PurchaseOrderItem item) {
        return new PurchaseOrderItemResponse(
                item.getId(),
                item.getSku().getId(),
                item.getSku().getSkuCode(),
                item.getSku().getSpu().getName(),
                item.getSku().getColorName(),
                item.getSku().getSizeName(),
                item.getQuantity(),
                item.getUnitCost(),
                item.getAmount()
        );
    }
}
