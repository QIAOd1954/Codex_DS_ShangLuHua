package com.shangluhua.app.auth;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "admin_user")
public class AdminUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String username;

    @Column(nullable = false, length = 256)
    private String password;

    private String displayName;
    private String role = "ADMIN";
    @Enumerated(EnumType.STRING)
    private AdminUserStatus status = AdminUserStatus.ACTIVE;
    private Instant createdAt = Instant.now();

    public AdminUser() {}
    public AdminUser(String username, String password, String displayName) {
        this.username = username;
        this.password = password;
        this.displayName = displayName;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getRole() { return role; }
    public AdminUserStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
