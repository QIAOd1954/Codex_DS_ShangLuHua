package com.shangluhua.app.customer;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shangluhua.app.common.ApiException;

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
    public List<Customer> search(String keyword) {
        List<Customer> customers;
        if (keyword == null || keyword.isBlank()) {
            customers = customerRepository.findAll();
        } else {
            customers = customerRepository.findTop50ByNameContainingIgnoreCaseOrPhoneContainingIgnoreCase(keyword, keyword);
        }
        return customers.stream().filter(customer -> !"DELETED".equals(customer.getStatus())).toList();
    }

    @Transactional(readOnly = true)
    public Customer get(Long id) {
        Customer customer = customerRepository.findById(id).orElseThrow(() -> new ApiException("Customer not found: " + id));
        if ("DELETED".equals(customer.getStatus())) {
            throw new ApiException("Customer not found: " + id);
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
        customer.setStatus("DELETED");
        customerRepository.save(customer);
    }
}