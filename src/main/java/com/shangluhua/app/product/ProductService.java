package com.shangluhua.app.product;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shangluhua.app.common.ApiException;

@Service
public class ProductService {
    private final ProductSpuRepository spuRepository;

    public ProductService(ProductSpuRepository spuRepository) {
        this.spuRepository = spuRepository;
    }

    @Transactional
    public ProductSpu create(CreateProductRequest request) {
        spuRepository.findByCode(request.code()).ifPresent(existing -> {
            throw new ApiException("Product code already exists: " + existing.getCode());
        });

        ProductSpu spu = new ProductSpu();
        applyCreateFields(spu, request);

        for (CreateProductRequest.SkuSpec spec : request.skus()) {
            ProductSku sku = new ProductSku();
            sku.setSkuCode(request.code() + "-" + spec.colorName() + "-" + spec.sizeName());
            sku.setBarcode(spec.barcode());
            sku.setColorName(spec.colorName());
            sku.setSizeName(spec.sizeName());
            sku.setRetailPrice(spu.getRetailPrice());
            sku.setWholesalePrice(spu.getWholesalePrice());
            sku.setCostPrice(spu.getCostPrice());
            spu.addSku(sku);
        }
        return spuRepository.save(spu);
    }

    @Transactional(readOnly = true)
    public List<ProductSpu> search(String keyword) {
        List<ProductSpu> products;
        if (keyword == null || keyword.isBlank()) {
            products = spuRepository.findAll();
        } else {
            products = spuRepository.findTop50ByCodeContainingIgnoreCaseOrNameContainingIgnoreCase(keyword, keyword);
        }
        return products.stream().filter(product -> !"DELETED".equals(product.getStatus())).toList();
    }

    @Transactional(readOnly = true)
    public ProductSpu get(Long id) {
        ProductSpu product = spuRepository.findById(id).orElseThrow(() -> new ApiException("Product not found: " + id));
        if ("DELETED".equals(product.getStatus())) {
            throw new ApiException("Product not found: " + id);
        }
        return product;
    }

    @Transactional
    public ProductSpu update(Long id, UpdateProductRequest request) {
        ProductSpu spu = get(id);
        spuRepository.findByCode(request.code()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new ApiException("Product code already exists: " + existing.getCode());
            }
        });
        spu.setCode(request.code());
        spu.setName(request.name());
        spu.setCategory(request.category());
        spu.setSeason(request.season());
        spu.setSupplierName(request.supplierName());
        spu.setRetailPrice(defaultMoney(request.retailPrice()));
        spu.setWholesalePrice(defaultMoney(request.wholesalePrice()));
        spu.setCostPrice(defaultMoney(request.costPrice()));
        if (request.status() != null && !request.status().isBlank()) {
            spu.setStatus(request.status());
        }
        for (ProductSku sku : spu.getSkus()) {
            sku.setRetailPrice(spu.getRetailPrice());
            sku.setWholesalePrice(spu.getWholesalePrice());
            sku.setCostPrice(spu.getCostPrice());
        }
        return spuRepository.save(spu);
    }

    @Transactional
    public void delete(Long id) {
        ProductSpu product = get(id);
        product.setStatus("DELETED");
        spuRepository.save(product);
    }

    private void applyCreateFields(ProductSpu spu, CreateProductRequest request) {
        spu.setCode(request.code());
        spu.setName(request.name());
        spu.setCategory(request.category());
        spu.setSeason(request.season());
        spu.setSupplierName(request.supplierName());
        spu.setRetailPrice(defaultMoney(request.retailPrice()));
        spu.setWholesalePrice(defaultMoney(request.wholesalePrice()));
        spu.setCostPrice(defaultMoney(request.costPrice()));
    }

    private BigDecimal defaultMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}