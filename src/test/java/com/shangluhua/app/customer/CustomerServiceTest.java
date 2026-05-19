package com.shangluhua.app.customer;

import com.shangluhua.app.common.ApiException;
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
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    private CustomerService customerService;

    private Customer activeCustomer;
    private Customer deletedCustomer;

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(customerRepository);

        activeCustomer = new Customer();
        ReflectionTestUtils.setField(activeCustomer, "id", 1L);
        activeCustomer.setName("张三");
        activeCustomer.setPhone("13800138000");
        activeCustomer.setStatus(CustomerStatus.ACTIVE);
        activeCustomer.setDebtBalance(new BigDecimal("0.00"));

        deletedCustomer = new Customer();
        ReflectionTestUtils.setField(deletedCustomer, "id", 2L);
        deletedCustomer.setName("李四（已删除）");
        deletedCustomer.setStatus(CustomerStatus.DELETED);
    }

    @Test
    void shouldCreateCustomer() {
        when(customerRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CreateCustomerRequest request = new CreateCustomerRequest("王五", "13900139000", "wangwu", "VIP");
        Customer result = customerService.create(request);
        assertEquals("王五", result.getName());
        assertEquals("13900139000", result.getPhone());
    }

    @Test
    void shouldThrowWhenGettingDeletedCustomer() {
        when(customerRepository.findById(2L)).thenReturn(Optional.of(deletedCustomer));
        assertThrows(ApiException.class, () -> customerService.get(2L));
    }

    @Test
    void shouldSoftDeleteCustomer() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(activeCustomer));
        customerService.delete(1L);
        verify(customerRepository).save(argThat(c -> c.getStatus() == CustomerStatus.DELETED));
    }

    @Test
    void shouldFilterDeletedCustomers() {
        when(customerRepository.findAll(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(activeCustomer, deletedCustomer)));

        List<Customer> results = customerService.search(null, 0, 100);
        assertEquals(1, results.size());
        assertEquals(CustomerStatus.ACTIVE, results.get(0).getStatus());
    }

    @Test
    void shouldUpdateCustomerDebt() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(activeCustomer));
        when(customerRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateCustomerRequest request = new UpdateCustomerRequest("张三", "13800138000", "zhangsan", "VIP", new BigDecimal("500.00"));
        Customer result = customerService.update(1L, request);
        assertEquals(new BigDecimal("500.00"), result.getDebtBalance());
    }
}
