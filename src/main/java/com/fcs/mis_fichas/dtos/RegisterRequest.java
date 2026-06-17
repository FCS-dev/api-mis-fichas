package com.fcs.mis_fichas.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de solicitud para registrar un nuevo usuario.
 */
@Schema(description = "DTO de solicitud para registrar un nuevo usuario")
public record RegisterRequest(
        @NotBlank @Email
        @Schema(description = "Correo electrónico del usuario", example = "user@example.com")
        String email,

        @NotBlank @Size(min = 6)
        @Schema(description = "Contraseña del usuario (mínimo 6 caracteres)", example = "password123")
        String password,

        @NotBlank
        @Schema(description = "Nombre completo del usuario", example = "Juan Pérez")
        String name
) {
}
