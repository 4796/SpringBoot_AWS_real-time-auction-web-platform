package com.finalbid.auction.ratelimit;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.time.Duration;
import java.util.function.Supplier;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final ProxyManager<String> proxyManager;

    public RateLimitingFilter(ProxyManager<String> proxyManager) {
        this.proxyManager = proxyManager;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String ip = request.getRemoteAddr();
        String path = request.getRequestURI();
        
        // Global limit: 200/min/IP
        if (!isAllowed("global:" + ip, 200, Duration.ofMinutes(1))) {
            send429(response);
            return;
        }

        // Bid limit: 30/min/userId
        if (path.startsWith("/api/auctions/") && path.endsWith("/bids") && "POST".equalsIgnoreCase(request.getMethod())) {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof String userId) {
                if (!isAllowed("bid:" + userId, 30, Duration.ofMinutes(1))) {
                    send429(response);
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAllowed(String key, int capacity, Duration period) {
        Supplier<BucketConfiguration> configSupplier = () -> BucketConfiguration.builder()
                .addLimit(Bandwidth.builder().capacity(capacity).refillGreedy(capacity, period).build())
                .build();
        
        Bucket bucket = proxyManager.builder().build(key, configSupplier);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        return probe.isConsumed();
    }

    private void send429(HttpServletResponse response) throws IOException {
        response.setStatus(429);
        response.setHeader("Retry-After", "60");
        response.setContentType("application/json");
        response.getWriter().write("{\"detail\": \"Too many requests\", \"status\": 429}");
    }
}
