package com.shangluhua.app.product;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record CreateProductRequest(
        @NotBlank String code,
        @NotBlank String name,
        String category,
        String season,
        String supplierName,
        BigDecimal retailPrice,
        BigDecimal wholesalePrice,
        BigDecimal costPrice,
        String imageUrl,
        @NotEmpty List<@Valid SkuSpec> skus
) {
    public record SkuSpec(@NotBlank String colorName, @NotBlank String sizeName, String barcode) {}
}
