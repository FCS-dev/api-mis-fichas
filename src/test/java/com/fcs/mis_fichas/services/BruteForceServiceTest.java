package com.fcs.mis_fichas.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

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

    @Test
    void getBlockedAccounts_shouldReturnBlockedEntries() {
        bruteForceService.recordFailedAttempt("alpha@example.com");
        bruteForceService.recordFailedAttempt("alpha@example.com");
        bruteForceService.recordFailedAttempt("alpha@example.com");
        bruteForceService.recordFailedAttempt("alpha@example.com");
        bruteForceService.recordFailedAttempt("alpha@example.com");

        bruteForceService.recordFailedAttempt("beta@example.com");
        bruteForceService.recordFailedAttempt("beta@example.com");
        bruteForceService.recordFailedAttempt("beta@example.com");

        List<BruteForceService.BlockedAccount> blocked = bruteForceService.getBlockedAccounts();

        assertThat(blocked).hasSize(1);
        assertThat(blocked.get(0).email()).isEqualTo("alpha@example.com");
        assertThat(blocked.get(0).lockedUntil()).isAfter(LocalDateTime.now());
    }

    @Test
    void getBlockedAccounts_shouldReturnEmpty_whenDisabled() {
        BruteForceService disabled = new BruteForceService(false, 5, 15);
        disabled.recordFailedAttempt("user@example.com");
        disabled.recordFailedAttempt("user@example.com");
        disabled.recordFailedAttempt("user@example.com");
        disabled.recordFailedAttempt("user@example.com");
        disabled.recordFailedAttempt("user@example.com");

        assertThat(disabled.getBlockedAccounts()).isEmpty();
    }

    @Test
    void getBlockedAccounts_shouldReturnEmpty_whenNoBlocked() {
        bruteForceService.recordFailedAttempt("user@example.com");
        bruteForceService.recordFailedAttempt("user@example.com");

        assertThat(bruteForceService.getBlockedAccounts()).isEmpty();
    }

    @Test
    void unblock_shouldReturnTrue_whenBlocked() {
        for (int i = 0; i < 5; i++) {
            bruteForceService.recordFailedAttempt("user@example.com");
        }

        assertThat(bruteForceService.isBlocked("user@example.com")).isTrue();

        boolean result = bruteForceService.unblock("user@example.com");

        assertThat(result).isTrue();
        assertThat(bruteForceService.isBlocked("user@example.com")).isFalse();
    }

    @Test
    void unblock_shouldReturnFalse_whenNotBlocked() {
        boolean result = bruteForceService.unblock("user@example.com");

        assertThat(result).isFalse();
    }

    @Test
    void sortBlockedAccounts_shouldSortByEmailAscendingByDefault() {
        BruteForceService.BlockedAccount a = new BruteForceService.BlockedAccount(
                "zebra@example.com", LocalDateTime.now().plusMinutes(15));
        BruteForceService.BlockedAccount b = new BruteForceService.BlockedAccount(
                "alpha@example.com", LocalDateTime.now().plusMinutes(5));

        List<BruteForceService.BlockedAccount> sorted = bruteForceService.sortBlockedAccounts(
                List.of(a, b), "email", true);

        assertThat(sorted).extracting(BruteForceService.BlockedAccount::email)
                .containsExactly("alpha@example.com", "zebra@example.com");
    }

    @Test
    void sortBlockedAccounts_shouldSortByLockedUntilDescending() {
        LocalDateTime now = LocalDateTime.now();
        BruteForceService.BlockedAccount a = new BruteForceService.BlockedAccount(
                "a@example.com", now.plusMinutes(5));
        BruteForceService.BlockedAccount b = new BruteForceService.BlockedAccount(
                "b@example.com", now.plusMinutes(15));

        List<BruteForceService.BlockedAccount> sorted = bruteForceService.sortBlockedAccounts(
                List.of(a, b), "lockedUntil", false);

        assertThat(sorted).extracting(BruteForceService.BlockedAccount::email)
                .containsExactly("b@example.com", "a@example.com");
    }
}
