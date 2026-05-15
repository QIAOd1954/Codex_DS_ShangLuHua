package com.shangluhua.app.product;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "product_spu")
public class ProductSpu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @Column(nullable = false, length = 128)
    private String name;

    private String category;
    private String season;
    private String supplierName;
    private BigDecimal retailPrice = BigDecimal.ZERO;
    private BigDecimal wholesalePrice = BigDecimal.ZERO;
    private BigDecimal costPrice = BigDecimal.ZERO;
    private String imageUrl;
    private String status = "ON_SALE";
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "spu", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ProductSku> skus = new ArrayList<>();

    public void addSku(ProductSku sku) {
        sku.setSpu(this);
        skus.add(sku);
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getSeason() { return season; }
    public void setSeason(String season) { this.season = season; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public BigDecimal getRetailPrice() { return retailPrice; }
    public void setRetailPrice(BigDecimal retailPrice) { this.retailPrice = retailPrice; }
    public BigDecimal getWholesalePrice() { return wholesalePrice; }
    public void setWholesalePrice(BigDecimal wholesalePrice) { this.wholesalePrice = wholesalePrice; }
    public BigDecimal getCostPrice() { return costPrice; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public List<ProductSku> getSkus() { return skus; }
}