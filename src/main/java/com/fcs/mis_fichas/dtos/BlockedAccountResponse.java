package com.fcs.mis_fichas.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Representa una cuenta bloqueada por protección contra fuerza bruta.
 */
@Schema(description = "Cuenta bloqueada por intentos fallidos de login")
public record BlockedAccountResponse(
        @Schema(description = "Email de la cuenta bloqueada", example = "usuario@example.com")
        String email,

        @Schema(description = "Fecha y hora tentativa en la que se habilitará nuevamente el login", example = "2026-09-16T15:30:00")
        LocalDateTime lockedUntil
) {
}
