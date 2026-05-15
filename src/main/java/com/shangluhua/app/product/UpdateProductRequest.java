package com.shangluhua.app.product;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;

public record UpdateProductRequest(
        @NotBlank String code,
        @NotBlank String name,
        String category,
        String season,
        String supplierName,
        BigDecimal retailPrice,
        BigDecimal wholesalePrice,
        BigDecimal costPrice,
        String imageUrl,
        String status
) {}
