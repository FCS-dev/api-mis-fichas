package com.fcs.mis_fichas.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Solicitud para revocar manualmente el bloqueo de una cuenta.
 */
@Schema(description = "Solicitud para desbloquear una cuenta por email")
public record UnblockRequest(
        @NotBlank(message = "El email es obligatorio")
        @Schema(description = "Email de la cuenta a desbloquear", example = "usuario@example.com")
        String email
) {
}
