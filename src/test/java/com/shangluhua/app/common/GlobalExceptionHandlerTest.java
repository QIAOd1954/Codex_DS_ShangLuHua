package com.shangluhua.app.common;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldReturn400ForApiException() {
        ApiException ex = new ApiException("测试错误");
        ResponseEntity<ApiResponse<Void>> response = handler.handleApiException(ex);
        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().success());
        assertEquals("测试错误", response.getBody().message());
    }

    @Test
    void shouldReturn400ForValidationError() {
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(new Object(), "object");
        errors.reject("name", "不能为空");
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, errors);

        ResponseEntity<ApiResponse<Void>> response = handler.handleValidation(ex);
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void shouldReturn500ForUnknownException() {
        Exception ex = new RuntimeException("内部错误");
        ResponseEntity<ApiResponse<Void>> response = handler.handleOther(ex);
        assertEquals(500, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().success());
        assertEquals("Internal server error", response.getBody().message());
    }
}
