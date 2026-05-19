package com.shangluhua.app.product;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
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
    @CacheEvict(value = "products", allEntries = true)
    public ProductSpu create(CreateProductRequest request) {
        spuRepository.findByCode(request.code()).ifPresent(existing -> {
            throw new ApiException("商品编码已存在: " + existing.getCode());
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
    @Cacheable(value = "products", key = "'search:' + #keyword + ':' + #page + ':' + #size", unless = "#result.isEmpty()")
    public List<ProductSpu> search(String keyword, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        List<ProductSpu> products;
        if (keyword == null || keyword.isBlank()) {
            products = spuRepository.findAll(pageable).getContent();
        } else {
            products = spuRepository.findByCodeContainingIgnoreCaseOrNameContainingIgnoreCase(keyword, keyword, pageable).getContent();
        }
        return products.stream().filter(product -> product.getStatus() != ProductStatus.DELETED).toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "'get:' + #id")
    public ProductSpu get(Long id) {
        ProductSpu product = spuRepository.findById(id).orElseThrow(() -> new ApiException("商品不存在: " + id));
        if (product.getStatus() == ProductStatus.DELETED) {
            throw new ApiException("商品不存在: " + id);
        }
        return product;
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductSpu update(Long id, UpdateProductRequest request) {
        ProductSpu spu = get(id);
        spuRepository.findByCode(request.code()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new ApiException("商品编码已存在: " + existing.getCode());
            }
        });
        spu.setCode(request.code());
        spu.setName(request.name());
        spu.setCategory(request.category());
        spu.setSeason(request.season());
        spu.setSupplierName(request.supplierName());
        spu.setImageUrl(request.imageUrl());
        spu.setRetailPrice(defaultMoney(request.retailPrice()));
        spu.setWholesalePrice(defaultMoney(request.wholesalePrice()));
        spu.setCostPrice(defaultMoney(request.costPrice()));
        if (request.status() != null) {
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
    @CacheEvict(value = "products", allEntries = true)
    public void delete(Long id) {
        ProductSpu product = get(id);
        product.setStatus(ProductStatus.DELETED);
        spuRepository.save(product);
    }

    private void applyCreateFields(ProductSpu spu, CreateProductRequest request) {
        spu.setCode(request.code());
        spu.setName(request.name());
        spu.setCategory(request.category());
        spu.setSeason(request.season());
        spu.setSupplierName(request.supplierName());
        spu.setImageUrl(request.imageUrl());
        spu.setRetailPrice(defaultMoney(request.retailPrice()));
        spu.setWholesalePrice(defaultMoney(request.wholesalePrice()));
        spu.setCostPrice(defaultMoney(request.costPrice()));
    }

    private BigDecimal defaultMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}