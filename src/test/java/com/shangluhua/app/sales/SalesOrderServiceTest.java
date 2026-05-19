package com.shangluhua.app.sales;

import com.shangluhua.app.common.ApiException;
import com.shangluhua.app.customer.Customer;
import com.shangluhua.app.customer.CustomerRepository;
import com.shangluhua.app.inventory.InventoryService;
import com.shangluhua.app.product.ProductSku;
import com.shangluhua.app.product.ProductSkuRepository;
import com.shangluhua.app.product.ProductSpu;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalesOrderServiceTest {

    @Mock
    private SalesOrderRepository orderRepository;
    @Mock
    private ProductSkuRepository skuRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private InventoryService inventoryService;

    private SalesOrderService salesOrderService;

    private ProductSku sku;
    private ProductSpu spu;
    private SalesOrder savedOrder;

    @BeforeEach
    void setUp() {
        salesOrderService = new SalesOrderService(orderRepository, skuRepository, customerRepository, inventoryService);

        spu = new ProductSpu();
        ReflectionTestUtils.setField(spu, "id", 1L);
        spu.setCode("TEST001");
        spu.setName("测试商品");
        spu.setWholesalePrice(new BigDecimal("100.00"));
        spu.setCostPrice(new BigDecimal("50.00"));

        sku = new ProductSku();
        ReflectionTestUtils.setField(sku, "id", 1L);
        sku.setSkuCode("TEST001-红色-M");
        sku.setSpu(spu);
        sku.setWholesalePrice(new BigDecimal("100.00"));
        sku.setCostPrice(new BigDecimal("50.00"));

        savedOrder = new SalesOrder();
        ReflectionTestUtils.setField(savedOrder, "id", 1L);
    }

    @Test
    void shouldRejectUnitPriceBelowWholesalePrice() {
        when(skuRepository.findById(1L)).thenReturn(Optional.of(sku));

        CreateSalesOrderRequest.Item item = new CreateSalesOrderRequest.Item(1L, 1, new BigDecimal("50.00"));
        CreateSalesOrderRequest request = new CreateSalesOrderRequest(
                null, null, BigDecimal.ZERO, "张三", "13800138000", List.of(item)
        );

        ApiException ex = assertThrows(ApiException.class, () -> salesOrderService.create(request));
        assertTrue(ex.getMessage().contains("不能低于批发价"));
    }

    @Test
    void shouldAcceptUnitPriceEqualToWholesalePrice() {
        when(skuRepository.findById(1L)).thenReturn(Optional.of(sku));
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CreateSalesOrderRequest.Item item = new CreateSalesOrderRequest.Item(1L, 1, new BigDecimal("100.00"));
        CreateSalesOrderRequest request = new CreateSalesOrderRequest(
                null, null, BigDecimal.ZERO, "张三", "13800138000", List.of(item)
        );

        assertDoesNotThrow(() -> salesOrderService.create(request));
    }

    @Test
    void shouldAcceptNullUnitPriceAndUseWholesalePrice() {
        when(skuRepository.findById(1L)).thenReturn(Optional.of(sku));
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CreateSalesOrderRequest.Item item = new CreateSalesOrderRequest.Item(1L, 1, null);
        CreateSalesOrderRequest request = new CreateSalesOrderRequest(
                null, null, BigDecimal.ZERO, "张三", "13800138000", List.of(item)
        );

        assertDoesNotThrow(() -> salesOrderService.create(request));
    }

    @Test
    void shouldRejectNegativeUnitPrice() {
        when(skuRepository.findById(1L)).thenReturn(Optional.of(sku));

        CreateSalesOrderRequest.Item item = new CreateSalesOrderRequest.Item(1L, 1, new BigDecimal("-10.00"));
        CreateSalesOrderRequest request = new CreateSalesOrderRequest(
                null, null, BigDecimal.ZERO, "张三", "13800138000", List.of(item)
        );

        ApiException ex = assertThrows(ApiException.class, () -> salesOrderService.create(request));
        assertTrue(ex.getMessage().contains("不能为负数"));
    }

    @Test
    void shouldRejectNegativePaidAmount() {
        CreateSalesOrderRequest.Item item = new CreateSalesOrderRequest.Item(1L, 1, new BigDecimal("100.00"));
        CreateSalesOrderRequest request = new CreateSalesOrderRequest(
                null, null, new BigDecimal("-100.00"), "张三", "13800138000", List.of(item)
        );

        ApiException ex = assertThrows(ApiException.class, () -> salesOrderService.create(request));
        assertTrue(ex.getMessage().contains("实付金额不能为负数"));
    }

    @Test
    void shouldCalculateDebtCorrectly() {
        when(skuRepository.findById(1L)).thenReturn(Optional.of(sku));
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CreateSalesOrderRequest.Item item = new CreateSalesOrderRequest.Item(1L, 1, new BigDecimal("100.00"));
        CreateSalesOrderRequest request = new CreateSalesOrderRequest(
                null, null, new BigDecimal("30.00"), "张三", "13800138000", List.of(item)
        );

        SalesOrder order = salesOrderService.create(request);
        assertEquals(new BigDecimal("70.00"), order.getDebtAmount());
        assertEquals(new BigDecimal("100.00"), order.getTotalAmount());
    }
}
