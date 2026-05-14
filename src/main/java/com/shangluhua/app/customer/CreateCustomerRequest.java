package com.shangluhua.app.customer;

import jakarta.validation.constraints.NotBlank;

public record CreateCustomerRequest(@NotBlank String name, String phone, String wechat, String levelName) {}
