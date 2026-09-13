package com.fcs.mis_fichas.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BruteForceServiceTest {

    private BruteForceService bruteForceService;

    @BeforeEach
    void setUp() {
        bruteForceService = new BruteForceService(true, 5, 15);
    }

    @Test
    void recordFailedAttempt_shouldCreateEntry_whenFirstFailure() {
        bruteForceService.recordFailedAttempt("user@example.com");

        assertThat(bruteForceService.isBlocked("user@example.com")).isFalse();
    }

    @Test
    void recordFailedAttempt_shouldIncrementAttempts() {
        bruteForceService.recordFailedAttempt("user@example.com");
        bruteForceService.recordFailedAttempt("user@example.com");

        assertThat(bruteForceService.isBlocked("user@example.com")).isFalse();
    }

    @Test
    void isBlocked_shouldReturnFalse_whenAttemptsBelowThreshold() {
        for (int i = 0; i < 4; i++) {
            bruteForceService.recordFailedAttempt("user@example.com");
        }

        assertThat(bruteForceService.isBlocked("user@example.com")).isFalse();
    }

    @Test
    void isBlocked_shouldReturnTrue_whenAttemptsReachThreshold() {
        for (int i = 0; i < 5; i++) {
            bruteForceService.recordFailedAttempt("user@example.com");
        }

        assertThat(bruteForceService.isBlocked("user@example.com")).isTrue();
    }

    @Test
    void isBlocked_shouldReturnFalse_whenNoEntry() {
        assertThat(bruteForceService.isBlocked("nonexistent@example.com")).isFalse();
    }

    @Test
    void resetAttempts_shouldClearEntry() {
        for (int i = 0; i < 4; i++) {
            bruteForceService.recordFailedAttempt("user@example.com");
        }

        bruteForceService.resetAttempts("user@example.com");

        assertThat(bruteForceService.isBlocked("user@example.com")).isFalse();
    }

    @Test
    void isBlocked_shouldReturnFalse_whenLockoutExpired() {
        BruteForceService shortLockout = new BruteForceService(true, 5, 0);
        for (int i = 0; i < 5; i++) {
            shortLockout.recordFailedAttempt("user@example.com");
        }

        assertThat(shortLockout.isBlocked("user@example.com")).isFalse();
    }

    @Test
    void isDisabled_shouldAlwaysReturnFalse() {
        BruteForceService disabled = new BruteForceService(false, 5, 15);

        for (int i = 0; i < 10; i++) {
            disabled.recordFailedAttempt("user@example.com");
        }

        assertThat(disabled.isBlocked("user@example.com")).isFalse();
    }
}
