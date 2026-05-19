package com.shangluhua.app.sales;

import com.shangluhua.app.common.ApiException;
import com.shangluhua.app.customer.Customer;
import com.shangluhua.app.customer.CustomerRepository;
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
class SalesOrderIntegrationTest {

    @Mock private SalesOrderRepository orderRepository;
    @Mock private ProductSkuRepository skuRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private InventoryService inventoryService;

    private SalesOrderService salesOrderService;
    private ProductSku sku;
    private SalesOrder order;
    private Customer customer;

    @BeforeEach
    void setUp() {
        salesOrderService = new SalesOrderService(orderRepository, skuRepository, customerRepository, inventoryService);

        ProductSpu spu = new ProductSpu();
        ReflectionTestUtils.setField(spu, "id", 1L);
        spu.setCode("T001");

        sku = new ProductSku();
        ReflectionTestUtils.setField(sku, "id", 1L);
        sku.setSkuCode("T001-红-M");
        sku.setSpu(spu);
        sku.setWholesalePrice(new BigDecimal("100.00"));
        sku.setCostPrice(new BigDecimal("60.00"));

        customer = new Customer();
        ReflectionTestUtils.setField(customer, "id", 1L);
        customer.setName("张三");
        customer.setDebtBalance(BigDecimal.ZERO);
    }

    @Test
    void shouldCreateAndConfirmAndCancelOrder() {
        // Create
        when(skuRepository.findById(1L)).thenReturn(Optional.of(sku));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CreateSalesOrderRequest.Item item = new CreateSalesOrderRequest.Item(1L, 2, new BigDecimal("100.00"));
        CreateSalesOrderRequest request = new CreateSalesOrderRequest(
                1L, "MAIN", new BigDecimal("50.00"), "张三", "13800138000", List.of(item));

        SalesOrder created = salesOrderService.create(request);
        assertEquals(SalesOrderStatus.DRAFT, created.getStatus());
        assertEquals(new BigDecimal("200.00"), created.getTotalAmount());
        assertEquals(new BigDecimal("150.00"), created.getDebtAmount());

        // Confirm
        SalesOrder persisted = new SalesOrder();
        ReflectionTestUtils.setField(persisted, "id", 1L);
        persisted.setStatus(SalesOrderStatus.DRAFT);
        persisted.setTotalAmount(new BigDecimal("200.00"));
        persisted.setPaidAmount(new BigDecimal("50.00"));
        persisted.setDebtAmount(new BigDecimal("150.00"));
        persisted.setWarehouseCode("MAIN");
        persisted.setCustomer(customer);
        persisted.addItem(created.getItems().get(0));

        when(orderRepository.lockWithItems(1L)).thenReturn(Optional.of(persisted));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SalesOrder confirmed = salesOrderService.confirm(1L);
        assertEquals(SalesOrderStatus.CONFIRMED, confirmed.getStatus());
        verify(inventoryService).adjust(eq(1L), eq("MAIN"), eq(-2), eq(InventoryBizType.SALES_ORDER), eq(1L));
        verify(customerRepository).save(argThat(c -> c.getDebtBalance().compareTo(new BigDecimal("150.00")) == 0));

        // Cancel
        persisted.setStatus(SalesOrderStatus.CONFIRMED);
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SalesOrder canceled = salesOrderService.cancel(1L);
        assertEquals(SalesOrderStatus.CANCELED, canceled.getStatus());
        verify(inventoryService).adjust(eq(1L), eq("MAIN"), eq(2), eq(InventoryBizType.SALES_CANCELED), eq(1L));
    }

    @Test
    void shouldNotConfirmNonDraftOrder() {
        SalesOrder confirmed = new SalesOrder();
        ReflectionTestUtils.setField(confirmed, "id", 1L);
        confirmed.setStatus(SalesOrderStatus.CONFIRMED);

        when(orderRepository.lockWithItems(1L)).thenReturn(Optional.of(confirmed));

        assertThrows(ApiException.class, () -> salesOrderService.confirm(1L));
    }

    @Test
    void shouldNotCancelNonConfirmedOrder() {
        SalesOrder draft = new SalesOrder();
        ReflectionTestUtils.setField(draft, "id", 1L);
        draft.setStatus(SalesOrderStatus.DRAFT);

        when(orderRepository.lockWithItems(1L)).thenReturn(Optional.of(draft));

        assertThrows(ApiException.class, () -> salesOrderService.cancel(1L));
    }
}
