package com.shangluhua.app.purchase;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreatePurchaseOrderRequest(
        String warehouseCode,
        @NotEmpty List<@Valid Item> items
) {
    public record Item(@NotNull Long skuId, @Positive int quantity) {}
}
