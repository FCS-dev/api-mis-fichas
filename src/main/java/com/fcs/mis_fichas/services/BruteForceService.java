package com.fcs.mis_fichas.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

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

    /**
     * Devuelve la lista de cuentas actualmente bloqueadas por fuerza bruta.
     * Cada entrada contiene el email y el momento hasta el cual permanece bloqueada.
     *
     * @return lista de cuentas bloqueadas
     */
    public List<BlockedAccount> getBlockedAccounts() {
        if (!enabled) {
            return List.of();
        }
        LocalDateTime now = LocalDateTime.now();
        return cache.asMap().entrySet().stream()
                .filter(entry -> {
                    LocalDateTime lockedUntil = entry.getValue().lockedUntil();
                    return lockedUntil != null && lockedUntil.isAfter(now);
                })
                .map(entry -> new BlockedAccount(entry.getKey(), entry.getValue().lockedUntil()))
                .toList();
    }

    /**
     * Revoca manualmente el bloqueo de una cuenta.
     *
     * @param email email de la cuenta a desbloquear
     * @return true si la cuenta estaba bloqueada y se desbloqueó, false en caso contrario
     */
    public boolean unblock(String email) {
        if (!enabled) {
            return false;
        }
        boolean wasBlocked = isBlocked(email);
        cache.invalidate(email);
        if (wasBlocked) {
            log.info("Bloqueo revocado manualmente para email: {}", email);
        }
        return wasBlocked;
    }

    /**
     * Ordena una lista de cuentas bloqueadas según la propiedad y dirección indicadas.
     * Propiedades soportadas: "email" y "lockedUntil".
     *
     * @param accounts  lista a ordenar
     * @param property  propiedad por la que ordenar
     * @param ascending true para ascendente, false para descendente
     * @return lista ordenada
     */
    public List<BlockedAccount> sortBlockedAccounts(List<BlockedAccount> accounts, String property, boolean ascending) {
        Comparator<BlockedAccount> comparator = switch (property) {
            case "lockedUntil" -> Comparator.comparing(BlockedAccount::lockedUntil);
            default -> Comparator.comparing(BlockedAccount::email, String.CASE_INSENSITIVE_ORDER);
        };
        if (!ascending) {
            comparator = comparator.reversed();
        }
        return accounts.stream().sorted(comparator).toList();
    }

    public record LoginAttempt(int failedAttempts, LocalDateTime lockedUntil) {
    }

    public record BlockedAccount(String email, LocalDateTime lockedUntil) {
    }
}
