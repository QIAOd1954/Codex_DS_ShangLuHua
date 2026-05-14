package com.shangluhua.app.inventory;

import java.time.Instant;

import com.shangluhua.app.product.ProductSku;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "inventory_ledger")
public class InventoryLedger {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String warehouseCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku_id", nullable = false)
    private ProductSku sku;

    private String bizType;
    private Long bizId;
    private int changeQuantity;
    private int beforeQuantity;
    private int afterQuantity;
    private Instant createdAt = Instant.now();

    public void setWarehouseCode(String warehouseCode) { this.warehouseCode = warehouseCode; }
    public void setSku(ProductSku sku) { this.sku = sku; }
    public void setBizType(String bizType) { this.bizType = bizType; }
    public void setBizId(Long bizId) { this.bizId = bizId; }
    public void setChangeQuantity(int changeQuantity) { this.changeQuantity = changeQuantity; }
    public void setBeforeQuantity(int beforeQuantity) { this.beforeQuantity = beforeQuantity; }
    public void setAfterQuantity(int afterQuantity) { this.afterQuantity = afterQuantity; }
}
