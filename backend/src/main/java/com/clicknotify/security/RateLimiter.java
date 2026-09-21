package com.clicknotify.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Minimal fixed-window rate limiter, kept in memory — no Redis, no extra
 * infrastructure, because this is a small project and a single instance
 * is all it needs to run. Its whole job is to stop one client from
 * spamming /notifications and burning through the real Gmail
 * quota this app calls out to.
 */
@Component
public class RateLimiter {

    @Value("${security.rate-limit.max-requests:10}")
    private int maxRequests;

    @Value("${security.rate-limit.window-seconds:60}")
    private int windowSeconds;

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    private static class Window {
        long windowStart;
        AtomicInteger count = new AtomicInteger(0);
        Window(long windowStart) { this.windowStart = windowStart; }
    }

    /** Returns true if this key (usually an IP) is still within its allowed rate. */
    public boolean allow(String key) {
        long now = System.currentTimeMillis();
        long windowMillis = windowSeconds * 1000L;

        Window window = windows.compute(key, (k, existing) -> {
            if (existing == null || now - existing.windowStart > windowMillis) {
                return new Window(now);
            }
            return existing;
        });

        return window.count.incrementAndGet() <= maxRequests;
    }
}
