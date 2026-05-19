package com.shangluhua.app.inventory;

import com.shangluhua.app.common.ApiException;
import com.shangluhua.app.product.ProductSku;
import com.shangluhua.app.product.ProductSkuRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryBalanceRepository balanceRepository;
    @Mock
    private InventoryLedgerRepository ledgerRepository;
    @Mock
    private ProductSkuRepository skuRepository;

    private InventoryService inventoryService;
    private ProductSku sku;

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryService(balanceRepository, ledgerRepository, skuRepository);
        sku = new ProductSku();
        ReflectionTestUtils.setField(sku, "id", 1L);
        sku.setSkuCode("TEST001-红色-M");
    }

    @Test
    void shouldThrowWhenSkuNotFound() {
        when(skuRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ApiException.class, () ->
                inventoryService.adjust(999L, "MAIN", 10, InventoryBizType.PURCHASE_IN, 1L));
    }

    @Test
    void shouldCreateBalanceOnFirstAdjust() {
        when(skuRepository.findById(1L)).thenReturn(Optional.of(sku));
        when(balanceRepository.lockByWarehouseCodeAndSkuId(anyString(), anyLong())).thenReturn(Optional.empty());
        when(balanceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(ledgerRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        InventoryBalance balance = inventoryService.adjust(1L, "MAIN", 10, InventoryBizType.PURCHASE_IN, 1L);
        assertNotNull(balance);
        assertEquals(10, balance.getQuantity());
    }

    @Test
    void shouldThrowWhenStockInsufficient() {
        when(skuRepository.findById(1L)).thenReturn(Optional.of(sku));

        InventoryBalance existing = new InventoryBalance();
        existing.setQuantity(3);
        existing.setWarehouseCode("MAIN");
        existing.setSku(sku);

        when(balanceRepository.lockByWarehouseCodeAndSkuId("MAIN", 1L)).thenReturn(Optional.of(existing));

        ApiException ex = assertThrows(ApiException.class, () ->
                inventoryService.adjust(1L, "MAIN", -5, InventoryBizType.SALES_ORDER, 1L));
        assertTrue(ex.getMessage().contains("库存不足"));
    }

    @Test
    void shouldAdjustStockCorrectly() {
        when(skuRepository.findById(1L)).thenReturn(Optional.of(sku));

        InventoryBalance existing = new InventoryBalance();
        existing.setQuantity(10);
        existing.setWarehouseCode("MAIN");
        existing.setSku(sku);

        when(balanceRepository.lockByWarehouseCodeAndSkuId("MAIN", 1L)).thenReturn(Optional.of(existing));
        when(balanceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(ledgerRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        InventoryBalance balance = inventoryService.adjust(1L, "MAIN", -3, InventoryBizType.SALES_ORDER, 1L);
        assertEquals(7, balance.getQuantity());
    }

    @Test
    void shouldNormalizeNullWarehouse() {
        when(skuRepository.findById(1L)).thenReturn(Optional.of(sku));
        when(balanceRepository.lockByWarehouseCodeAndSkuId("MAIN", 1L)).thenReturn(Optional.empty());
        when(balanceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(ledgerRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        InventoryBalance balance = inventoryService.adjust(1L, null, 5, InventoryBizType.PURCHASE_IN, 1L);
        assertEquals("MAIN", balance.getWarehouseCode());
    }

    @Test
    void shouldRecordLedgerOnAdjust() {
        when(skuRepository.findById(1L)).thenReturn(Optional.of(sku));

        InventoryBalance existing = new InventoryBalance();
        existing.setQuantity(10);
        existing.setWarehouseCode("MAIN");
        existing.setSku(sku);

        when(balanceRepository.lockByWarehouseCodeAndSkuId("MAIN", 1L)).thenReturn(Optional.of(existing));
        when(balanceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(ledgerRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        inventoryService.adjust(1L, "MAIN", -3, InventoryBizType.SALES_ORDER, 1L);
        verify(ledgerRepository).save(argThat(ledger ->
                ledger.getChangeQuantity() == -3 &&
                ledger.getBeforeQuantity() == 10 &&
                ledger.getAfterQuantity() == 7 &&
                ledger.getBizType() == InventoryBizType.SALES_ORDER
        ));
    }
}
