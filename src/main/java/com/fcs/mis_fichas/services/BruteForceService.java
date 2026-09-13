package com.fcs.mis_fichas.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
public class BruteForceService {

    private final Cache<String, LoginAttempt> cache;
    private final boolean enabled;
    private final int maxAttempts;
    private final int lockoutMinutes;

    public BruteForceService(
            @Value("${security.protection.enabled:true}") boolean enabled,
            @Value("${brute-force.max-attempts:5}") int maxAttempts,
            @Value("${brute-force.lockout-minutes:15}") int lockoutMinutes) {
        this.enabled = enabled;
        this.maxAttempts = maxAttempts;
        this.lockoutMinutes = lockoutMinutes;
        this.cache = Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(Duration.ofMinutes(30))
                .build();
        log.info("BruteForceService initialized: enabled={}, maxAttempts={}, lockoutMinutes={}",
                enabled, maxAttempts, lockoutMinutes);
    }

    public boolean isBlocked(String email) {
        if (!enabled) return false;

        LoginAttempt attempt = cache.getIfPresent(email);
        if (attempt == null) return false;

        if (attempt.lockedUntil() != null && attempt.lockedUntil().isAfter(LocalDateTime.now())) {
            log.warn("Blocked login attempt for email: {}", email);
            return true;
        }

        cache.invalidate(email);
        return false;
    }

    public void recordFailedAttempt(String email) {
        if (!enabled) return;

        LoginAttempt existing = cache.getIfPresent(email);
        int attempts = (existing != null) ? existing.failedAttempts() + 1 : 1;
        LocalDateTime lockedUntil = (attempts >= maxAttempts)
                ? LocalDateTime.now().plusMinutes(lockoutMinutes)
                : null;

        cache.put(email, new LoginAttempt(attempts, lockedUntil));

        if (lockedUntil != null) {
            log.warn("Account locked for email: {} ({} failed attempts, locked until {})",
                    email, attempts, lockedUntil);
        } else {
            log.info("Failed login attempt for email: {} ({}/{})", email, attempts, maxAttempts);
        }
    }

    public void resetAttempts(String email) {
        if (!enabled) return;
        cache.invalidate(email);
        log.info("Login attempts reset for email: {}", email);
    }

    public record LoginAttempt(int failedAttempts, LocalDateTime lockedUntil) {
    }
}
