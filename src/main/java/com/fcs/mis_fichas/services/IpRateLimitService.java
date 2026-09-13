package com.fcs.mis_fichas.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class IpRateLimitService {

    private final Cache<String, RateLimitEntry> cache;
    private final boolean enabled;
    private final int maxRequests;
    private final long windowMillis;

    public IpRateLimitService(
            @Value("${security.protection.enabled:true}") boolean enabled,
            @Value("${rate-limit.max-requests:20}") int maxRequests,
            @Value("${rate-limit.window-seconds:60}") int windowSeconds) {
        this.enabled = enabled;
        this.maxRequests = maxRequests;
        this.windowMillis = windowSeconds * 1000L;
        this.cache = Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(Duration.ofMinutes(5))
                .build();
        log.info("IpRateLimitService initialized: enabled={}, maxRequests={}, windowSeconds={}",
                enabled, maxRequests, windowSeconds);
    }

    public boolean isAllowed(String ip) {
        if (!enabled) return true;

        long now = System.currentTimeMillis();
        RateLimitEntry entry = cache.get(ip, k -> new RateLimitEntry(new AtomicInteger(0), now));

        if (now - entry.windowStart() > windowMillis) {
            entry = new RateLimitEntry(new AtomicInteger(0), now);
            cache.put(ip, entry);
        }

        int currentCount = entry.count().incrementAndGet();
        if (currentCount > maxRequests) {
            log.warn("Rate limit exceeded for IP: {}/{}", ip, currentCount);
            return false;
        }

        return true;
    }

    public record RateLimitEntry(AtomicInteger count, long windowStart) {
    }
}
