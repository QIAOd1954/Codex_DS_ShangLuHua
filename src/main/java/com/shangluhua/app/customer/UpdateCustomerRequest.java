package com.shangluhua.app.customer;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;

public record UpdateCustomerRequest(
        @NotBlank String name,
        String phone,
        String wechat,
        String levelName,
        BigDecimal debtBalance
) {}