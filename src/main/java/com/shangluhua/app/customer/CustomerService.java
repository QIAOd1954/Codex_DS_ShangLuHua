package com.shangluhua.app.customer;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shangluhua.app.common.ApiException;
import com.shangluhua.app.customer.CustomerStatus;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public Customer create(CreateCustomerRequest request) {
        Customer customer = new Customer();
        customer.setName(request.name());
        customer.setPhone(request.phone());
        customer.setWechat(request.wechat());
        if (request.levelName() != null && !request.levelName().isBlank()) {
            customer.setLevelName(request.levelName());
        }
        return customerRepository.save(customer);
    }

    @Transactional(readOnly = true)
    public List<Customer> search(String keyword, int page, int size) {
        List<Customer> customers;
        PageRequest pageable = PageRequest.of(page, size);
        if (keyword == null || keyword.isBlank()) {
            customers = customerRepository.findAll(pageable).getContent();
        } else {
            customers = customerRepository.findByNameContainingIgnoreCaseOrPhoneContainingIgnoreCase(keyword, keyword, pageable).getContent();
        }
        return customers.stream().filter(customer -> customer.getStatus() != CustomerStatus.DELETED).toList();
    }

    @Transactional(readOnly = true)
    public Customer get(Long id) {
        Customer customer = customerRepository.findById(id).orElseThrow(() -> new ApiException("客户不存在: " + id));
        if (customer.getStatus() == CustomerStatus.DELETED) {
            throw new ApiException("客户不存在: " + id);
        }
        return customer;
    }

    @Transactional
    public Customer update(Long id, UpdateCustomerRequest request) {
        Customer customer = get(id);
        customer.setName(request.name());
        customer.setPhone(request.phone());
        customer.setWechat(request.wechat());
        if (request.levelName() != null && !request.levelName().isBlank()) {
            customer.setLevelName(request.levelName());
        }
        customer.setDebtBalance(request.debtBalance() == null ? BigDecimal.ZERO : request.debtBalance());
        return customerRepository.save(customer);
    }

    @Transactional
    public void delete(Long id) {
        Customer customer = get(id);
        customer.setStatus(CustomerStatus.DELETED);
        customerRepository.save(customer);
    }
}