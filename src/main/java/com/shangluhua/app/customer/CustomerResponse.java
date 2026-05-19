package com.shangluhua.app.customer;

import java.math.BigDecimal;

public record CustomerResponse(
        Long id,
        String name,
        String phone,
        String wechat,
        String levelName,
        BigDecimal debtBalance,
        CustomerStatus status
) {
    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getPhone(),
                customer.getWechat(),
                customer.getLevelName(),
                customer.getDebtBalance(),
                customer.getStatus()
        );
    }
}