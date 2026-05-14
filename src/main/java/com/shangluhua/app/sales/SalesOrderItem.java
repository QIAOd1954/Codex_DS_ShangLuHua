package com.shangluhua.app.sales;

import java.math.BigDecimal;

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
@Table(name = "sales_order_item")
public class SalesOrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private SalesOrder order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku_id", nullable = false)
    private ProductSku sku;

    private String spuCodeSnapshot;
    private String productNameSnapshot;
    private String colorSnapshot;
    private String sizeSnapshot;
    private int quantity;
    private BigDecimal unitPrice = BigDecimal.ZERO;
    private BigDecimal costPriceSnapshot = BigDecimal.ZERO;
    private BigDecimal amount = BigDecimal.ZERO;

    public Long getId() { return id; }
    public String getProductNameSnapshot() { return productNameSnapshot; }
    public String getColorSnapshot() { return colorSnapshot; }
    public String getSizeSnapshot() { return sizeSnapshot; }

    public ProductSku getSku() { return sku; }
    public void setSku(ProductSku sku) { this.sku = sku; }
    public void setOrder(SalesOrder order) { this.order = order; }
    public String getSpuCodeSnapshot() { return spuCodeSnapshot; }
    public void setSpuCodeSnapshot(String spuCodeSnapshot) { this.spuCodeSnapshot = spuCodeSnapshot; }
    public void setProductNameSnapshot(String productNameSnapshot) { this.productNameSnapshot = productNameSnapshot; }
    public void setColorSnapshot(String colorSnapshot) { this.colorSnapshot = colorSnapshot; }
    public void setSizeSnapshot(String sizeSnapshot) { this.sizeSnapshot = sizeSnapshot; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getCostPriceSnapshot() { return costPriceSnapshot; }
    public void setCostPriceSnapshot(BigDecimal costPriceSnapshot) { this.costPriceSnapshot = costPriceSnapshot; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
