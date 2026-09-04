package com.fcs.mis_fichas.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", "my-super-secret-key-that-is-long-enough-for-hs256-algorithm");
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 900000L); // 15 min
    }

    @Test
    void generateAccessToken_shouldReturnValidToken() {
        String token = jwtService.generateAccessToken("user@example.com", "USER", "Test User", 1L);

        assertThat(token).isNotNull();
        assertThat(token).isNotBlank();
    }

    @Test
    void validateToken_shouldReturnTrue_forValidToken() {
        String token = jwtService.generateAccessToken("user@example.com", "USER", "Test User", 1L);

        assertThat(jwtService.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_shouldReturnFalse_forInvalidToken() {
        assertThat(jwtService.validateToken("invalid.token.here")).isFalse();
    }

    @Test
    void validateToken_shouldReturnFalse_forTamperedToken() {
        String token = jwtService.generateAccessToken("user@example.com", "USER", "Test User", 1L);
        String tampered = token.substring(0, token.length() - 1) + "x";

        assertThat(jwtService.validateToken(tampered)).isFalse();
    }

    @Test
    void extractEmail_shouldReturnSubject() {
        String token = jwtService.generateAccessToken("user@example.com", "USER", "Test User", 1L);

        assertThat(jwtService.extractEmail(token)).isEqualTo("user@example.com");
    }

    @Test
    void extractRole_shouldReturnRoleClaim() {
        String token = jwtService.generateAccessToken("user@example.com", "ADMIN", "Admin User", 1L);

        assertThat(jwtService.extractRole(token)).isEqualTo("ADMIN");
    }
}
