package com.fcs.mis_fichas.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de respuesta para la autenticación.
 * Contiene el access token JWT y, opcionalmente, el refresh token.
 */
@Schema(description = "DTO de respuesta para la autenticación")
public record AuthResponse(
        @Schema(description = "Token JWT de acceso de corta duración", example = "eyJhbGciOiJIUzI1NiIs...")
        String accessToken,

        @Schema(description = "Token de refresco de larga duración (puede ser null)", example = "a1b2c3d4e5f6...")
        String refreshToken
) {
}
