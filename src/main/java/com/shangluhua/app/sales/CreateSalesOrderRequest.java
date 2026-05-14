package com.shangluhua.app.sales;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateSalesOrderRequest(
        Long customerId,
        String warehouseCode,
        BigDecimal paidAmount,
        String contactName,
        String contactPhone,
        @NotEmpty List<@Valid Item> items
) {
    public record Item(@NotNull Long skuId, @Positive int quantity, BigDecimal unitPrice) {}
}
