package com.fcs.mis_fichas.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.function.Supplier;

@Slf4j
@Service
public class IdempotencyService {

    private final Cache<String, IdempotencyEntry> cache;

    public IdempotencyService(IdempotencyProperties properties) {
        this.cache = Caffeine.newBuilder()
                .maximumSize(properties.getMaxEntries())
                .expireAfterWrite(properties.getTtl())
                .build();
        log.info("Idempotency cache initialized: maxEntries={}, ttl={}", properties.getMaxEntries(), properties.getTtl());
    }

    public <T> T processRequest(String idempotencyKey, String bodyHash, Supplier<T> businessLogic) {
        IdempotencyEntry existing = cache.getIfPresent(idempotencyKey);

        if (existing == null) {
            T response = businessLogic.get();
            cache.put(idempotencyKey, new IdempotencyEntry(bodyHash, response));
            return response;
        }

        if (!existing.bodyHash().equals(bodyHash)) {
            throw new IdempotencyConflictException(
                    "Idempotency key '" + idempotencyKey + "' already used with a different request body"
            );
        }

        log.debug("Returning cached response for idempotency key: {}", idempotencyKey);
        @SuppressWarnings("unchecked")
        T cachedResponse = (T) existing.response();
        return cachedResponse;
    }

    public static String computeBodyHash(String body, Long userId) {
        String input = body + "|" + userId;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    public record IdempotencyEntry(String bodyHash, Object response) {
    }
}
