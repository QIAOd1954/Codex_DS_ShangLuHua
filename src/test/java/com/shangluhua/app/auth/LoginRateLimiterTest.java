package com.shangluhua.app.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.shangluhua.app.common.ApiException;

import static org.junit.jupiter.api.Assertions.*;

class LoginRateLimiterTest {

    private LoginRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        rateLimiter = new LoginRateLimiter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void shouldAllowFirstAttempt() {
        assertDoesNotThrow(() -> rateLimiter.check());
    }

    @Test
    void shouldAllowUpToFiveAttempts() {
        for (int i = 0; i < 5; i++) {
            assertDoesNotThrow(() -> rateLimiter.check(), "第 " + (i + 1) + " 次尝试应被允许");
        }
    }

    @Test
    void shouldBlockOnSixthAttempt() {
        for (int i = 0; i < 5; i++) {
            rateLimiter.check();
        }
        ApiException ex = assertThrows(ApiException.class, () -> rateLimiter.check());
        assertTrue(ex.getMessage().contains("登录尝试过于频繁"));
    }

    @Test
    void shouldResetAfterSuccessfulLogin() {
        for (int i = 0; i < 5; i++) {
            rateLimiter.check();
        }
        rateLimiter.reset();
        assertDoesNotThrow(() -> rateLimiter.check(), "重置后应允许新尝试");
    }

    @Test
    void differentIpsShouldNotAffectEachOther() throws InterruptedException {
        // Thread 1: use 127.0.0.1
        Thread t1 = new Thread(() -> {
            MockHttpServletRequest req = new MockHttpServletRequest();
            req.setRemoteAddr("127.0.0.1");
            RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(req));
            for (int i = 0; i < 6; i++) {
                try { rateLimiter.check(); } catch (ApiException ignored) {}
            }
        });

        // Thread 2: use 192.168.1.1
        Thread t2 = new Thread(() -> {
            MockHttpServletRequest req = new MockHttpServletRequest();
            req.setRemoteAddr("192.168.1.1");
            RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(req));
            for (int i = 0; i < 5; i++) {
                assertDoesNotThrow(() -> rateLimiter.check());
            }
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }
}
