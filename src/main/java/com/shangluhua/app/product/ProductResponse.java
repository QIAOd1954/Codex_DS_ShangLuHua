package com.shangluhua.app.product;

import java.math.BigDecimal;
import java.util.List;

public record ProductResponse(
        Long id,
        String code,
        String name,
        String category,
        String season,
        String supplierName,
        BigDecimal retailPrice,
        BigDecimal wholesalePrice,
        BigDecimal costPrice,
        String status,
        List<ProductSkuResponse> skus
) {
    public static ProductResponse from(ProductSpu spu) {
        return new ProductResponse(
                spu.getId(),
                spu.getCode(),
                spu.getName(),
                spu.getCategory(),
                spu.getSeason(),
                spu.getSupplierName(),
                spu.getRetailPrice(),
                spu.getWholesalePrice(),
                spu.getCostPrice(),
                spu.getStatus(),
                spu.getSkus().stream().map(ProductSkuResponse::from).toList()
        );
    }
}