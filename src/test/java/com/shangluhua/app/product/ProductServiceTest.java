package com.shangluhua.app.product;

import com.shangluhua.app.common.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductSpuRepository spuRepository;

    private ProductService productService;

    private ProductSpu activeSpu;
    private ProductSpu deletedSpu;

    @BeforeEach
    void setUp() {
        productService = new ProductService(spuRepository);

        activeSpu = new ProductSpu();
        ReflectionTestUtils.setField(activeSpu, "id", 1L);
        activeSpu.setCode("P001");
        activeSpu.setName("测试商品A");
        activeSpu.setStatus(ProductStatus.ON_SALE);
        activeSpu.setRetailPrice(new BigDecimal("200.00"));
        activeSpu.setWholesalePrice(new BigDecimal("150.00"));
        activeSpu.setCostPrice(new BigDecimal("100.00"));

        deletedSpu = new ProductSpu();
        ReflectionTestUtils.setField(deletedSpu, "id", 2L);
        deletedSpu.setCode("P002");
        deletedSpu.setName("已删除商品");
        deletedSpu.setStatus(ProductStatus.DELETED);
    }

    @Test
    void shouldCreateProduct() {
        when(spuRepository.findByCode("P003")).thenReturn(Optional.empty());
        when(spuRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CreateProductRequest.SkuSpec skuSpec = new CreateProductRequest.SkuSpec("红色", "M", "6901234567890");
        CreateProductRequest request = new CreateProductRequest("P003", "新商品", "女装", "2026春", "供应商A",
                new BigDecimal("200.00"), new BigDecimal("150.00"), new BigDecimal("100.00"),
                "http://example.com/img.jpg", List.of(skuSpec));

        ProductSpu result = productService.create(request);
        assertNotNull(result);
        assertEquals("P003", result.getCode());
        assertEquals("新商品", result.getName());
    }

    @Test
    void shouldThrowWhenCodeExists() {
        when(spuRepository.findByCode("P001")).thenReturn(Optional.of(activeSpu));

        CreateProductRequest request = new CreateProductRequest("P001", "重复商品", "女装", "2026春", "供应商A",
                null, null, null, null, List.of());

        ApiException ex = assertThrows(ApiException.class, () -> productService.create(request));
        assertTrue(ex.getMessage().contains("商品编码已存在"));
    }

    @Test
    void shouldFilterDeletedProductsInSearch() {
        when(spuRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(activeSpu, deletedSpu)));

        List<ProductSpu> results = productService.search(null, 0, 100);
        assertEquals(1, results.size());
        assertEquals(ProductStatus.ON_SALE, results.get(0).getStatus());
    }

    @Test
    void shouldGetActiveProduct() {
        when(spuRepository.findById(1L)).thenReturn(Optional.of(activeSpu));
        assertDoesNotThrow(() -> productService.get(1L));
    }

    @Test
    void shouldThrowWhenGettingDeletedProduct() {
        when(spuRepository.findById(2L)).thenReturn(Optional.of(deletedSpu));
        ApiException ex = assertThrows(ApiException.class, () -> productService.get(2L));
        assertTrue(ex.getMessage().contains("不存在"));
    }

    @Test
    void shouldThrowWhenGettingNonExistentProduct() {
        when(spuRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ApiException.class, () -> productService.get(999L));
    }

    @Test
    void shouldSoftDeleteProduct() {
        when(spuRepository.findById(1L)).thenReturn(Optional.of(activeSpu));
        productService.delete(1L);
        verify(spuRepository).save(argThat(spu -> spu.getStatus() == ProductStatus.DELETED));
    }
}
