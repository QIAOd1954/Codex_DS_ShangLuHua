package com.shangluhua.app.customer;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findTop50ByNameContainingIgnoreCaseOrPhoneContainingIgnoreCase(String name, String phone);
}
