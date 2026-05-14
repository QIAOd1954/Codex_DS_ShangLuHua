package com.shangluhua.app.auth;

import com.shangluhua.app.common.ApiException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(AdminUserRepository adminUserRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        if (adminUserRepository.count() == 0) {
            AdminUser defaultUser = new AdminUser("admin", passwordEncoder.encode("123456"), "系统管理员");
            adminUserRepository.save(defaultUser);
        }
        AdminUser user = adminUserRepository.findByUsername(request.username())
                .orElseThrow(() -> new ApiException("用户名或密码错误"));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ApiException("用户名或密码错误");
        }
        if (!"ACTIVE".equals(user.getStatus())) {
            throw new ApiException("账号已被禁用");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return ResponseEntity.ok(new LoginResponse(token, user.getUsername(), user.getDisplayName(), user.getRole()));
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException("未登录");
        }
        var principal = (JwtAuthFilter.AdminUserPrincipal) authentication.getPrincipal();
        AdminUser user = adminUserRepository.findById(principal.userId())
                .orElseThrow(() -> new ApiException("用户不存在"));
        return ResponseEntity.ok(Map.of(
                "username", user.getUsername(),
                "displayName", user.getDisplayName(),
                "role", user.getRole()
        ));
    }
}
