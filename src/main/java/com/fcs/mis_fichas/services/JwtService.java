package com.fcs.mis_fichas.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Servicio de gestion de tokens JWT (JSON Web Tokens).
 * Responsable de generar, validar y extraer informacion de los tokens de acceso.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    /**
     * Genera la clave de firma HMAC-SHA256 a partir del secreto configurado.
     *
     * @return clave secreta para firmar y verificar tokens
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Genera un nuevo access token JWT.
     * El token incluye el email como subject y el rol como claim adicional.
     *
     * @param email correo electronico del usuario (subject del token)
     * @param role  rol del usuario (claim "role")
     * @return token JWT firmado
     */
    public String generateAccessToken(String email, String role) {
        Instant now = Instant.now();
        Instant expiration = now.plus(accessTokenExpiration, ChronoUnit.MILLIS);

        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Valida un token JWT verificando su firma y expiracion.
     *
     * @param token token JWT a validar
     * @return true si el token es valido, false en caso contrario
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extrae el email (subject) de un token JWT.
     *
     * @param token token JWT
     * @return correo electronico del usuario
     */
    public String extractEmail(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    /**
     * Extrae el rol del usuario desde un token JWT.
     *
     * @param token token JWT
     * @return rol del usuario (ej: "USER", "ADMIN")
     */
    public String extractRole(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get("role", String.class);
    }
}
