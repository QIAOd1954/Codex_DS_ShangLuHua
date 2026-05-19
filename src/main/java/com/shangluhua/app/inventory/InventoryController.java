package com.shangluhua.app.inventory;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/adjust")
    public InventoryBalance adjust(@Valid @RequestBody AdjustInventoryRequest request) {
        return inventoryService.adjust(request.skuId(), request.warehouseCode(), request.changeQuantity(), InventoryBizType.MANUAL_ADJUST, null);
    }

    @GetMapping("/{skuId}")
    public InventoryBalance get(@PathVariable Long skuId, @RequestParam(required = false) String warehouseCode) {
        return inventoryService.getBalance(skuId, warehouseCode);
    }
}
