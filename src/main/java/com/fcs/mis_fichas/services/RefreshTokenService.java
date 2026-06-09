package com.fcs.mis_fichas.services;

import com.fcs.mis_fichas.entities.RefreshToken;
import com.fcs.mis_fichas.entities.User;
import com.fcs.mis_fichas.repositories.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    private static final int TOKEN_LENGTH = 64;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public String createRefreshToken(User user) {
        String token = generateRandomToken();
        String hash = hashToken(token);

        Instant now = Instant.now();
        Instant expiration = now.plus(refreshTokenExpiration, ChronoUnit.MILLIS);

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(user)
                .tokenHash(hash)
                .expiresAt(LocalDateTime.ofInstant(expiration, java.time.ZoneId.systemDefault()))
                .revokedAt(null)
                .build();

        refreshTokenRepository.save(refreshToken);
        return token;
    }

    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByTokenHash(String token) {
        return refreshTokenRepository.findByTokenHash(hashToken(token));
    }

    @Transactional
    public String rotateRefreshToken(String oldToken) {
        String oldHash = hashToken(oldToken);
        RefreshToken oldRefreshToken = refreshTokenRepository.findByTokenHash(oldHash)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (oldRefreshToken.getRevokedAt() != null) {
            log.warn("Refresh token reuse detected for user: {}", oldRefreshToken.getUserId().getEmail());
            revokeAllUserTokens(oldRefreshToken.getUserId().getId());
            throw new RuntimeException("Refresh token was revoked. All tokens cleared for security.");
        }

        if (oldRefreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh token expired");
        }

        oldRefreshToken.setRevokedAt(LocalDateTime.now());
        refreshTokenRepository.save(oldRefreshToken);

        return createRefreshToken(oldRefreshToken.getUserId());
    }

    @Transactional
    public void revokeRefreshToken(String token) {
        String hash = hashToken(token);
        refreshTokenRepository.findByTokenHash(hash).ifPresent(refreshToken -> {
            refreshToken.setRevokedAt(LocalDateTime.now());
            refreshTokenRepository.save(refreshToken);
            log.info("Refresh token revoked for user: {}", refreshToken.getUserId().getEmail());
        });
    }

    @Transactional
    public void revokeAllUserTokens(Long userId) {
        refreshTokenRepository.findByUserIdAndRevokedAtIsNull(userId)
                .ifPresent(token -> {
                    token.setRevokedAt(LocalDateTime.now());
                    refreshTokenRepository.save(token);
                });
    }

    private String generateRandomToken() {
        byte[] bytes = new byte[TOKEN_LENGTH];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
