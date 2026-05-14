package com.shangluhua.app.inventory;

import java.time.Instant;

import com.shangluhua.app.product.ProductSku;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "inventory_balance", uniqueConstraints = @UniqueConstraint(columnNames = {"warehouse_code", "sku_id"}))
public class InventoryBalance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "warehouse_code")
    private String warehouseCode = "MAIN";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku_id", nullable = false)
    private ProductSku sku;

    private int quantity;
    private int lockedQuantity;
    private Instant updatedAt = Instant.now();

    public Long getId() { return id; }
    public String getWarehouseCode() { return warehouseCode; }
    public void setWarehouseCode(String warehouseCode) { this.warehouseCode = warehouseCode; }
    public ProductSku getSku() { return sku; }
    public void setSku(ProductSku sku) { this.sku = sku; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public int getLockedQuantity() { return lockedQuantity; }
    public void setLockedQuantity(int lockedQuantity) { this.lockedQuantity = lockedQuantity; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void touch() { this.updatedAt = Instant.now(); }
}