package com.fcs.mis_fichas.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO de solicitud para iniciar sesión.
 */
@Schema(description = "DTO de solicitud para iniciar sesión")
public record LoginRequest(
        @NotBlank @Email
        @Schema(description = "Correo electrónico del usuario", example = "user@example.com")
        String email,

        @NotBlank
        @Schema(description = "Contraseña del usuario", example = "password123")
        String password
) {
}
