package com.fcs.mis_fichas.dtos;

import com.fcs.mis_fichas.enums.Role;
import com.fcs.mis_fichas.enums.Status;

import java.time.LocalDateTime;

/**
 * DTO de respuesta que representa un usuario del sistema.
 * No incluye el hash de la contraseña por seguridad.
 *
 * @param id        identificador único del usuario
 * @param email     correo electrónico del usuario
 * @param name      nombre del usuario
 * @param role      rol del usuario (USER o ADMIN)
 * @param status    estado de la cuenta (ACTIVE o BLOCKED)
 * @param createdAt fecha de creación
 * @param updatedAt fecha de última actualización
 * @param deletedAt fecha de eliminación lógica (soft delete)
 */
public record UserResponse(
        Long id,
        String email,
        String name,
        Role role,
        Status status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime deletedAt
) {
}
