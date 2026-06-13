package com.fcs.mis_fichas.dtos;

import com.fcs.mis_fichas.enums.Type;

import java.time.LocalDateTime;

/**
 * DTO de respuesta que representa una categoria.
 * Incluye información del creador y los datos de auditoria.
 *
 * @param id             identificador único de la categoria
 * @param name           nombre de la categoria
 * @param type           tipo de la categoria (INCOME o EXPENSE)
 * @param createdById    identificador del usuario creador
 * @param createdByEmail correo del usuario creador
 * @param createdAt      fecha de creación
 * @param updatedAt      fecha de última actualización
 * @param deletedAt      fecha de eliminación lógica
 */
public record CategoryResponse(
        Long id,
        String name,
        Type type,
        Long createdById,
        String createdByEmail,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime deletedAt
) {
}
