package com.shangluhua.app.auth;

public record LoginResponse(String token, String username, String displayName, String role) {}
