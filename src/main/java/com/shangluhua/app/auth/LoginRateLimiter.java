package com.shangluhua.app.auth;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.shangluhua.app.common.ApiException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_SECONDS = 60;

    private final Map<String, AttemptWindow> attempts = new ConcurrentHashMap<>();

    public void check() {
        String ip = getClientIp();
        if (ip == null) return;

        AttemptWindow window = attempts.compute(ip, (key, existing) -> {
            Instant now = Instant.now();
            if (existing == null || now.getEpochSecond() - existing.windowStart > WINDOW_SECONDS) {
                return new AttemptWindow(1, now.getEpochSecond());
            }
            if (existing.count >= MAX_ATTEMPTS) {
                return new AttemptWindow(existing.count + 1, existing.windowStart);
            }
            return new AttemptWindow(existing.count + 1, existing.windowStart);
        });

        if (window.count > MAX_ATTEMPTS) {
            long retryAfter = WINDOW_SECONDS - (Instant.now().getEpochSecond() - window.windowStart);
            throw new ApiException("登录尝试过于频繁，请 " + retryAfter + " 秒后重试");
        }
    }

    public void reset() {
        String ip = getClientIp();
        if (ip != null) {
            attempts.remove(ip);
        }
    }

    private String getClientIp() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return null;
        String xff = attrs.getRequest().getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return attrs.getRequest().getRemoteAddr();
    }

    private record AttemptWindow(int count, long windowStart) {}
}
