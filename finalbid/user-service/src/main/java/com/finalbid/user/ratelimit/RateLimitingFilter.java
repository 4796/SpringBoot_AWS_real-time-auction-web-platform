package com.finalbid.user.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.function.Supplier;

/**
 * Distributed rate limiting filter for user-service using Bucket4j + Redis.
 *
 * Rules (spec Section 5.1 / 9.4):
 *   POST /api/auth/login    → 10 req/min per IP
 *   POST /api/auth/register → 5 req/min per IP
 *   All other               → 200 req/min per IP
 *
 * On limit exceeded: HTTP 429 + Retry-After: 60
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final LettuceBasedProxyManager<String> proxyManager;

    public RateLimitingFilter(LettuceBasedProxyManager<String> proxyManager) {
        this.proxyManager = proxyManager;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest  request,
                                    HttpServletResponse response,
                                    FilterChain         filterChain)
            throws ServletException, IOException {

        String clientIp  = getClientIp(request);
        String method     = request.getMethod();
        String path       = request.getRequestURI();

        String bucketKey;
        Supplier<BucketConfiguration> configSupplier;

        if ("POST".equalsIgnoreCase(method) && "/api/auth/login".equals(path)) {
            bucketKey      = "rate_limit:login:" + clientIp;
            configSupplier = () -> loginConfig();
        } else if ("POST".equalsIgnoreCase(method) && "/api/auth/register".equals(path)) {
            bucketKey      = "rate_limit:register:" + clientIp;
            configSupplier = () -> registerConfig();
        } else {
            bucketKey      = "rate_limit:global:" + clientIp;
            configSupplier = () -> globalConfig();
        }

        var bucket = proxyManager.builder().build(bucketKey, configSupplier);

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", "60");
            response.setContentType("application/json");
            response.getWriter().write(
                "{\"status\":429,\"title\":\"Too Many Requests\"," +
                "\"detail\":\"Rate limit exceeded. Retry after 60 seconds.\"}"
            );
        }
    }

    // ── Bucket configurations ──────────────────────────────────────────────

    private BucketConfiguration loginConfig() {
        return BucketConfiguration.builder()
            .addLimit(Bandwidth.builder()
                .capacity(10)
                .refillGreedy(10, Duration.ofMinutes(1))
                .build())
            .build();
    }

    private BucketConfiguration registerConfig() {
        return BucketConfiguration.builder()
            .addLimit(Bandwidth.builder()
                .capacity(5)
                .refillGreedy(5, Duration.ofMinutes(1))
                .build())
            .build();
    }

    private BucketConfiguration globalConfig() {
        return BucketConfiguration.builder()
            .addLimit(Bandwidth.builder()
                .capacity(200)
                .refillGreedy(200, Duration.ofMinutes(1))
                .build())
            .build();
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}
