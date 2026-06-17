package com.fcs.mis_fichas.dtos;

import com.fcs.mis_fichas.enums.Role;
import com.fcs.mis_fichas.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * DTO de respuesta que representa un usuario del sistema.
 * No incluye el hash de la contraseña por seguridad.
 */
@Schema(description = "DTO de respuesta que representa un usuario del sistema")
public record UserResponse(
        @Schema(description = "Identificador único del usuario", example = "1")
        Long id,

        @Schema(description = "Correo electrónico del usuario", example = "user@example.com")
        String email,

        @Schema(description = "Nombre del usuario", example = "Juan Pérez")
        String name,

        @Schema(description = "Rol del usuario (USER o ADMIN)", example = "USER")
        Role role,

        @Schema(description = "Estado de la cuenta (ACTIVE o BLOCKED)", example = "ACTIVE")
        Status status,

        @Schema(description = "Fecha de creación", example = "2024-01-01T10:00:00")
        LocalDateTime createdAt,

        @Schema(description = "Fecha de última actualización", example = "2024-01-10T12:00:00")
        LocalDateTime updatedAt,

        @Schema(description = "Fecha de eliminación lógica (soft delete)", example = "null")
        LocalDateTime deletedAt
) {
}
