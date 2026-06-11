package com.fcs.mis_fichas.dtos;

/**
 * DTO de respuesta para la autenticacion.
 * Contiene el access token JWT y, opcionalmente, el refresh token.
 *
 * @param accessToken  token JWT de acceso de corta duracion
 * @param refreshToken token de refresco de larga duracion (puede ser null)
 */
public record AuthResponse(String accessToken, String refreshToken) {
}
