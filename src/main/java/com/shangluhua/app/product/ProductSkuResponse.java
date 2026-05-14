package com.shangluhua.app.product;

import java.math.BigDecimal;

public record ProductSkuResponse(
        Long id,
        String skuCode,
        String barcode,
        String colorName,
        String sizeName,
        BigDecimal retailPrice,
        BigDecimal wholesalePrice,
        BigDecimal costPrice
) {
    public static ProductSkuResponse from(ProductSku sku) {
        return new ProductSkuResponse(
                sku.getId(),
                sku.getSkuCode(),
                sku.getBarcode(),
                sku.getColorName(),
                sku.getSizeName(),
                sku.getRetailPrice(),
                sku.getWholesalePrice(),
                sku.getCostPrice()
        );
    }
}