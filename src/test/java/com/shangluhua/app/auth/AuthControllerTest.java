package com.shangluhua.app.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminUserRepository adminUserRepository;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private LoginRateLimiter rateLimiter;

    @MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Test
    void shouldReturn401ForInvalidCredentials() throws Exception {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        AdminUser user = new AdminUser("admin", encoder.encode("123456"), "管理员");
        ReflectionTestUtils.setField(user, "status", AdminUserStatus.ACTIVE);

        when(adminUserRepository.count()).thenReturn(1L);
        when(adminUserRepository.findByUsername("admin")).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        Map<String, String> body = Map.of("username", "admin", "password", "wrong");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));
    }

    @Test
    void shouldReturn200ForValidCredentials() throws Exception {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        AdminUser user = new AdminUser("admin", encoder.encode("123456"), "管理员");
        ReflectionTestUtils.setField(user, "status", AdminUserStatus.ACTIVE);

        when(adminUserRepository.count()).thenReturn(1L);
        when(adminUserRepository.findByUsername("admin")).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(user.getId(), user.getUsername())).thenReturn("test-jwt-token");

        Map<String, String> body = Map.of("username", "admin", "password", "123456");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-jwt-token"))
                .andExpect(jsonPath("$.username").value("admin"));
    }
}
