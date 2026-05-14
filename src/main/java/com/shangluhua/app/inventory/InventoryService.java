package com.shangluhua.app.inventory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shangluhua.app.common.ApiException;
import com.shangluhua.app.product.ProductSku;
import com.shangluhua.app.product.ProductSkuRepository;

@Service
public class InventoryService {
    private final InventoryBalanceRepository balanceRepository;
    private final InventoryLedgerRepository ledgerRepository;
    private final ProductSkuRepository skuRepository;

    public InventoryService(InventoryBalanceRepository balanceRepository,
                            InventoryLedgerRepository ledgerRepository,
                            ProductSkuRepository skuRepository) {
        this.balanceRepository = balanceRepository;
        this.ledgerRepository = ledgerRepository;
        this.skuRepository = skuRepository;
    }

    @Transactional
    public InventoryBalance adjust(Long skuId, String warehouseCode, int changeQuantity, String bizType, Long bizId) {
        ProductSku sku = skuRepository.findById(skuId).orElseThrow(() -> new ApiException("SKU not found: " + skuId));
        String warehouse = normalizeWarehouse(warehouseCode);
        InventoryBalance balance = balanceRepository.lockByWarehouseCodeAndSkuId(warehouse, skuId)
                .orElseGet(() -> createBalance(warehouse, sku));

        int before = balance.getQuantity();
        int after = before + changeQuantity;
        if (after < 0) {
            throw new ApiException("Insufficient inventory. Current " + before + ", change " + changeQuantity);
        }

        balance.setQuantity(after);
        balance.touch();
        InventoryBalance saved = balanceRepository.save(balance);

        InventoryLedger ledger = new InventoryLedger();
        ledger.setWarehouseCode(warehouse);
        ledger.setSku(sku);
        ledger.setBizType(bizType);
        ledger.setBizId(bizId);
        ledger.setChangeQuantity(changeQuantity);
        ledger.setBeforeQuantity(before);
        ledger.setAfterQuantity(after);
        ledgerRepository.save(ledger);

        return saved;
    }

    public InventoryBalance getBalance(Long skuId, String warehouseCode) {
        return balanceRepository.findByWarehouseCodeAndSkuId(normalizeWarehouse(warehouseCode), skuId)
                .orElseThrow(() -> new ApiException("Inventory balance not found"));
    }

    private InventoryBalance createBalance(String warehouseCode, ProductSku sku) {
        InventoryBalance balance = new InventoryBalance();
        balance.setWarehouseCode(warehouseCode);
        balance.setSku(sku);
        balance.setQuantity(0);
        return balance;
    }

    private String normalizeWarehouse(String warehouseCode) {
        return warehouseCode == null || warehouseCode.isBlank() ? "MAIN" : warehouseCode;
    }
}