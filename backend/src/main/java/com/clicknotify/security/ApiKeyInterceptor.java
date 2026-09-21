package com.clicknotify.security;

import com.clicknotify.exception.InvalidApiKeyException;
import com.clicknotify.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Deliberately small: a pre-shared API key plus a per-IP rate limit,
 * nothing more. This isn't trying to be SecureGallery's JWT/ownership
 * model — there's no concept of "users" here, just one client (the
 * frontend) that should hold a key, and a cap on how often anyone can
 * trigger a real email send.
 *
 * Note: this is a Spring HandlerInterceptor, not a raw servlet Filter.
 * Exceptions thrown here (unlike ones thrown in a Filter — see the
 * SecurityFilter bug fixed in SecureGallery) ARE caught by
 * @RestControllerAdvice, because interceptors run inside DispatcherServlet's
 * own request handling, which is what @ExceptionHandler resolution covers.
 * That's why this stays exception-based instead of writing raw JSON here.
 */
@Component
public class ApiKeyInterceptor implements HandlerInterceptor {

    @Value("${security.api-key}")
    private String expectedApiKey;

    private final RateLimiter rateLimiter;

    public ApiKeyInterceptor(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Allow CORS preflight without API key
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String ip = extractIp(request);

        // Only rate-limit state-changing requests (POST) — the rate limit
        // exists to stop someone spamming real email/Telegram sends, not to
        // cap how often the frontend can poll a GET status endpoint (e.g.
        // /telegram/link/{token}/status, checked every couple of seconds
        // while waiting for the user to tap Start in Telegram).
        if ("POST".equalsIgnoreCase(request.getMethod()) && !rateLimiter.allow(ip)) {
            throw new RateLimitExceededException(
                    "Too many requests — please wait a minute before trying again");
        }

        String providedKey = request.getHeader("X-API-Key");
        if (providedKey == null || !providedKey.equals(expectedApiKey)) {
            throw new InvalidApiKeyException("Missing or invalid API key");
        }

        return true;
    }

    private String extractIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isBlank()) {
            return xfHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
