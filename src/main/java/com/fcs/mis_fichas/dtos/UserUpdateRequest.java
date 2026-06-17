package com.fcs.mis_fichas.dtos;

import com.fcs.mis_fichas.enums.Role;
import com.fcs.mis_fichas.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de solicitud para actualizar un usuario existente.
 * Permite al ADMIN modificar nombre, email, rol y estado.
 * La contraseña no se modifica desde este endpoint.
 */
@Schema(description = "DTO de solicitud para actualizar un usuario existente")
public record UserUpdateRequest(
        @NotBlank(message = "Name is required")
        @Schema(description = "Nombre del usuario", example = "Juan Pérez")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Schema(description = "Correo electrónico del usuario", example = "user@example.com")
        String email,

        @NotNull(message = "Role is required")
        @Schema(description = "Rol del usuario (USER o ADMIN)", example = "USER")
        Role role,

        @NotNull(message = "Status is required")
        @Schema(description = "Estado de la cuenta (ACTIVE o BLOCKED)", example = "ACTIVE")
        Status status
) {
}
