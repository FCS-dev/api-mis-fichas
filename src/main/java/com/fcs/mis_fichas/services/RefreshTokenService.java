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

/**
 * Servicio de gestion de tokens de refresco (refresh tokens).
 * Implementa rotacion de tokens: cada uso revoca el anterior y genera uno nuevo.
 * Si se detecta reutilizacion de un token revocado, se revocan todos los tokens del usuario.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    private static final int TOKEN_LENGTH = 64;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Crea un nuevo token de refresco para un usuario.
     * Genera un token aleatorio criptograficamente seguro, lo hashea con SHA-256
     * y almacena el hash en la base de datos.
     *
     * @param user usuario al que se asigna el token
     * @return token de refresco en texto plano (se devuelve una sola vez al cliente)
     */
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

    /**
     * Busca un token de refresco por su valor en texto plano.
     * Internamente hashea el token para comparar con el almacenado.
     *
     * @param token token de refresco en texto plano
     * @return Optional con la entidad RefreshToken encontrada
     */
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByTokenHash(String token) {
        return refreshTokenRepository.findByTokenHash(hashToken(token));
    }

    /**
     * Rota un token de refresco: valida el token anterior, lo revoca y genera uno nuevo.
     * Si el token ya estaba revocado (posible reutilizacion maliciosa), revoca todos los tokens del usuario.
     *
     * @param oldToken token de refresco anterior en texto plano
     * @return nuevo token de refresco en texto plano
     * @throws RuntimeException si el token no existe, ya fue revocado o esta expirado
     */
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

    /**
     * Revoca un token de refresco especifico.
     *
     * @param token token de refresco en texto plano a revocar
     */
    @Transactional
    public void revokeRefreshToken(String token) {
        String hash = hashToken(token);
        refreshTokenRepository.findByTokenHash(hash).ifPresent(refreshToken -> {
            refreshToken.setRevokedAt(LocalDateTime.now());
            refreshTokenRepository.save(refreshToken);
            log.info("Refresh token revoked for user: {}", refreshToken.getUserId().getEmail());
        });
    }

    /**
     * Revoca todos los tokens activos de un usuario.
     * Se utiliza en caso de deteccion de reutilizacion de tokens o al cerrar sesion.
     *
     * @param userId identificador del usuario
     */
    @Transactional
    public void revokeAllUserTokens(Long userId) {
        refreshTokenRepository.findByUserIdAndRevokedAtIsNull(userId)
                .ifPresent(token -> {
                    token.setRevokedAt(LocalDateTime.now());
                    refreshTokenRepository.save(token);
                });
    }

    /**
     * Genera un token aleatorio criptograficamente seguro de 64 bytes.
     *
     * @return token aleatorio codificado en Base64 URL-safe
     */
    private String generateRandomToken() {
        byte[] bytes = new byte[TOKEN_LENGTH];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Hashea un token usando SHA-256.
     *
     * @param token token en texto plano
     * @return hash del token en Base64
     * @throws RuntimeException si el algoritmo SHA-256 no esta disponible
     */
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
