package com.fcs.mis_fichas.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IdempotencyServiceTest {

    private IdempotencyService idempotencyService;

    @BeforeEach
    void setUp() {
        IdempotencyProperties properties = new IdempotencyProperties();
        properties.setMaxEntries(100);
        idempotencyService = new IdempotencyService(properties);
    }

    @Test
    void processRequest_shouldExecuteAndCache_whenKeyNew() {
        String key = UUID.randomUUID().toString();
        String hash = IdempotencyService.computeBodyHash("body", 1L);

        String result = idempotencyService.processRequest(key, hash, () -> "response");

        assertThat(result).isEqualTo("response");
    }

    @Test
    void processRequest_shouldReturnCached_whenKeyExistsWithSameHash() {
        String key = UUID.randomUUID().toString();
        String hash = IdempotencyService.computeBodyHash("body", 1L);

        String first = idempotencyService.processRequest(key, hash, () -> "first");
        String second = idempotencyService.processRequest(key, hash, () -> "second");

        assertThat(first).isEqualTo("first");
        assertThat(second).isEqualTo("first");
    }

    @Test
    void processRequest_shouldThrow409_whenKeyExistsWithDifferentHash() {
        String key = UUID.randomUUID().toString();
        String hash1 = IdempotencyService.computeBodyHash("body1", 1L);
        String hash2 = IdempotencyService.computeBodyHash("body2", 1L);

        idempotencyService.processRequest(key, hash1, () -> "response");

        assertThatThrownBy(() -> idempotencyService.processRequest(key, hash2, () -> "response"))
                .isInstanceOf(IdempotencyConflictException.class)
                .hasMessageContaining("already used with a different request body");
    }

    @Test
    void processRequest_shouldCallBusinessLogicOnlyOnce_whenDuplicateRequest() {
        String key = UUID.randomUUID().toString();
        String hash = IdempotencyService.computeBodyHash("body", 1L);
        AtomicInteger counter = new AtomicInteger(0);

        idempotencyService.processRequest(key, hash, counter::incrementAndGet);
        idempotencyService.processRequest(key, hash, counter::incrementAndGet);

        assertThat(counter.get()).isEqualTo(1);
    }

    @Test
    void computeBodyHash_shouldReturnConsistentHash() {
        String hash1 = IdempotencyService.computeBodyHash("test body", 1L);
        String hash2 = IdempotencyService.computeBodyHash("test body", 1L);

        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    void computeBodyHash_shouldReturnDifferentHash_whenBodyDiffers() {
        String hash1 = IdempotencyService.computeBodyHash("body A", 1L);
        String hash2 = IdempotencyService.computeBodyHash("body B", 1L);

        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    void computeBodyHash_shouldReturnDifferentHash_whenUserDiffers() {
        String hash1 = IdempotencyService.computeBodyHash("body", 1L);
        String hash2 = IdempotencyService.computeBodyHash("body", 2L);

        assertThat(hash1).isNotEqualTo(hash2);
    }
}
