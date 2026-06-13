package com.fcs.mis_fichas.dtos;

/**
 * DTO de respuesta para la autenticación.
 * Contiene el access token JWT y, opcionalmente, el refresh token.
 *
 * @param accessToken  token JWT de acceso de corta duración
 * @param refreshToken token de refresco de larga duración (puede ser null)
 */
public record AuthResponse(String accessToken, String refreshToken) {
}
