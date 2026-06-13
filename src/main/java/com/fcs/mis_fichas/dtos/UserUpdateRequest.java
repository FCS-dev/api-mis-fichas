package com.fcs.mis_fichas.dtos;

import com.fcs.mis_fichas.enums.Role;
import com.fcs.mis_fichas.enums.Status;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de solicitud para actualizar un usuario existente.
 * Permite al ADMIN modificar nombre, email, rol y estado.
 * La contraseña no se modifica desde este endpoint.
 *
 * @param name   nombre del usuario (obligatorio)
 * @param email  correo electrónico (obligatorio, único)
 * @param role   rol del usuario (obligatorio)
 * @param status estado de la cuenta (obligatorio)
 */
public record UserUpdateRequest(
        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotNull(message = "Role is required")
        Role role,

        @NotNull(message = "Status is required")
        Status status
) {
}
