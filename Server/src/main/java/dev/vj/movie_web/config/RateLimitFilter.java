package dev.vj.movie_web.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Per-client fixed-window rate limiter (in-memory, single instance).
 * Writes (review submissions) get a much tighter budget than reads to curb spam.
 * For horizontally scaled deployments, move this to the gateway or a shared store (e.g. Redis).
 */
@Component
// Runs after the Spring Security chain (order -100) so CORS headers are present on 429 responses.
@Order(0)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final long WINDOW_MILLIS = 60_000;
    private static final int MAX_TRACKED_CLIENTS = 50_000;

    private final AppProperties.RateLimit config;
    private final ObjectMapper objectMapper;
    private final Clock clock = Clock.systemUTC();
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public RateLimitFilter(AppProperties props, ObjectMapper objectMapper) {
        this.config = props.rateLimit();
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !config.enabled()
                || !request.getRequestURI().startsWith("/api/")
                || "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        boolean write = !"GET".equalsIgnoreCase(request.getMethod()) && !"HEAD".equalsIgnoreCase(request.getMethod());
        int limit = write ? config.writeRequestsPerMinute() : config.readRequestsPerMinute();
        // remoteAddr honours X-Forwarded-For only when server.forward-headers-strategy is enabled (trusted proxy).
        String key = (write ? "w:" : "r:") + request.getRemoteAddr();

        long now = clock.millis();
        evictIfNeeded(now);
        Window window = windows.compute(key, (k, w) -> (w == null || now - w.start >= WINDOW_MILLIS) ? new Window(now) : w);
        int count = window.count.incrementAndGet();

        long resetSeconds = Math.max(1, (window.start + WINDOW_MILLIS - now + 999) / 1000);
        response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, limit - count)));

        if (count > limit) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", String.valueOf(resetSeconds));
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS,
                    "Too many requests. Please retry later.");
            objectMapper.writeValue(response.getOutputStream(), problem);
            return;
        }
        chain.doFilter(request, response);
    }

    private void evictIfNeeded(long now) {
        if (windows.size() > MAX_TRACKED_CLIENTS) {
            windows.entrySet().removeIf(e -> now - e.getValue().start >= WINDOW_MILLIS);
        }
    }

    private static final class Window {
        final long start;
        final AtomicInteger count = new AtomicInteger();

        Window(long start) {
            this.start = start;
        }
    }
}
