package com.fcs.mis_fichas.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IpRateLimitServiceTest {

    private IpRateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        rateLimitService = new IpRateLimitService(true, 20, 60);
    }

    @Test
    void isAllowed_shouldReturnTrue_whenFirstRequest() {
        assertThat(rateLimitService.isAllowed("192.168.1.1")).isTrue();
    }

    @Test
    void isAllowed_shouldReturnTrue_whenUnderLimit() {
        for (int i = 0; i < 19; i++) {
            rateLimitService.isAllowed("192.168.1.1");
        }

        assertThat(rateLimitService.isAllowed("192.168.1.1")).isTrue();
    }

    @Test
    void isAllowed_shouldReturnFalse_whenAtLimit() {
        for (int i = 0; i < 20; i++) {
            rateLimitService.isAllowed("192.168.1.1");
        }

        assertThat(rateLimitService.isAllowed("192.168.1.1")).isFalse();
    }

    @Test
    void isAllowed_shouldReturnTrue_whenWindowExpired() throws InterruptedException {
        IpRateLimitService shortWindow = new IpRateLimitService(true, 5, 1);

        for (int i = 0; i < 5; i++) {
            shortWindow.isAllowed("192.168.1.1");
        }
        assertThat(shortWindow.isAllowed("192.168.1.1")).isFalse();

        Thread.sleep(1100);

        assertThat(shortWindow.isAllowed("192.168.1.1")).isTrue();
    }

    @Test
    void isAllowed_shouldResetCount_afterWindowReset() throws InterruptedException {
        IpRateLimitService shortWindow = new IpRateLimitService(true, 3, 1);

        for (int i = 0; i < 3; i++) {
            shortWindow.isAllowed("192.168.1.1");
        }
        assertThat(shortWindow.isAllowed("192.168.1.1")).isFalse();

        Thread.sleep(1100);

        assertThat(shortWindow.isAllowed("192.168.1.1")).isTrue();
    }

    @Test
    void isDisabled_shouldAlwaysReturnTrue() {
        IpRateLimitService disabled = new IpRateLimitService(false, 1, 60);

        for (int i = 0; i < 100; i++) {
            assertThat(disabled.isAllowed("192.168.1.1")).isTrue();
        }
    }
}
