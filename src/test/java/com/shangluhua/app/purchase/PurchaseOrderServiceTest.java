package com.shangluhua.app.purchase;

import com.shangluhua.app.common.ApiException;
import com.shangluhua.app.inventory.InventoryBizType;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseOrderServiceTest {

    @Mock private PurchaseOrderRepository orderRepository;
    @Mock private ProductSkuRepository skuRepository;
    @Mock private InventoryService inventoryService;

    private PurchaseOrderService purchaseOrderService;
    private ProductSku sku;

    @BeforeEach
    void setUp() {
        purchaseOrderService = new PurchaseOrderService(orderRepository, skuRepository, inventoryService);

        ProductSpu spu = new ProductSpu();
        ReflectionTestUtils.setField(spu, "id", 1L);
        spu.setCode("P001");
        spu.setName("测试商品");
        spu.setSupplierName("供应商A");

        sku = new ProductSku();
        ReflectionTestUtils.setField(sku, "id", 1L);
        sku.setSkuCode("P001-红-M");
        sku.setSpu(spu);
        sku.setCostPrice(new BigDecimal("80.00"));
    }

    @Test
    void shouldCreatePurchaseOrder() {
        when(skuRepository.findById(1L)).thenReturn(Optional.of(sku));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CreatePurchaseOrderRequest.Item item = new CreatePurchaseOrderRequest.Item(1L, 10);
        CreatePurchaseOrderRequest request = new CreatePurchaseOrderRequest("MAIN", List.of(item));

        PurchaseOrder order = purchaseOrderService.create(request);
        assertEquals(PurchaseOrderStatus.DRAFT, order.getStatus());
        assertEquals(new BigDecimal("800.00"), order.getTotalCost());
        assertEquals("供应商A", order.getSupplierName());
    }

    @Test
    void shouldConfirmAndAddInventory() {
        when(orderRepository.lockWithItems(1L)).thenReturn(Optional.of(createDraftOrder()));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PurchaseOrder confirmed = purchaseOrderService.confirm(1L);
        assertEquals(PurchaseOrderStatus.CONFIRMED, confirmed.getStatus());
        verify(inventoryService).adjust(eq(1L), eq("MAIN"), eq(10), eq(InventoryBizType.PURCHASE_IN), eq(1L));
    }

    @Test
    void shouldCancelAndRestoreInventory() {
        PurchaseOrder confirmedOrder = createDraftOrder();
        confirmedOrder.setStatus(PurchaseOrderStatus.CONFIRMED);
        when(orderRepository.lockWithItems(1L)).thenReturn(Optional.of(confirmedOrder));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PurchaseOrder canceled = purchaseOrderService.cancel(1L);
        assertEquals(PurchaseOrderStatus.CANCELED, canceled.getStatus());
        verify(inventoryService).adjust(eq(1L), eq("MAIN"), eq(-10), eq(InventoryBizType.PURCHASE_CANCELED), eq(1L));
    }

    @Test
    void shouldNotConfirmNonDraft() {
        PurchaseOrder confirmed = createDraftOrder();
        confirmed.setStatus(PurchaseOrderStatus.CONFIRMED);
        when(orderRepository.lockWithItems(1L)).thenReturn(Optional.of(confirmed));

        assertThrows(ApiException.class, () -> purchaseOrderService.confirm(1L));
    }

    private PurchaseOrder createDraftOrder() {
        PurchaseOrder order = new PurchaseOrder();
        ReflectionTestUtils.setField(order, "id", 1L);
        order.setStatus(PurchaseOrderStatus.DRAFT);
        order.setWarehouseCode("MAIN");
        order.setTotalCost(new BigDecimal("800.00"));

        PurchaseOrderItem item = new PurchaseOrderItem();
        item.setSku(sku);
        item.setQuantity(10);
        item.setUnitCost(new BigDecimal("80.00"));
        item.setAmount(new BigDecimal("800.00"));
        order.addItem(item);

        return order;
    }
}
