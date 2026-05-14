package com.shangluhua.app.inventory;

import jakarta.validation.constraints.NotNull;

public record AdjustInventoryRequest(@NotNull Long skuId, String warehouseCode, int changeQuantity, String reason) {}
