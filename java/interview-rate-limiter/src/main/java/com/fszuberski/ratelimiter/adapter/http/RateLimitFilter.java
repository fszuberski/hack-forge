package com.fszuberski.ratelimiter.adapter.http;

import com.fszuberski.ratelimiter.core.RateLimiter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.web.filter.OncePerRequestFilter;

/** Registered by Spring Boot, deliberately not enforcing anything yet. */
public final class RateLimitFilter extends OncePerRequestFilter {
    private final RateLimiter rateLimiter;

    public RateLimitFilter(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
        // TODO: scope to /api/**, validate/strip X-API-Key, and call rateLimiter once.
        // TODO: map a denial to JSON 429 + rounded-up Retry-After; do not invoke the chain.
        // GET /health must stay exempt. See README for the complete target contract.
        filterChain.doFilter(request, response);
    }
}
