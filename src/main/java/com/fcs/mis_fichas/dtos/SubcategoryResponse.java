package com.fcs.mis_fichas.dtos;

import java.time.LocalDateTime;

/**
 * DTO de respuesta que representa una subcategoría.
 * Incluye información de la categoria padre, el creador y los datos de auditoria.
 *
 * @param id             identificador único de la subcategoría
 * @param name           nombre de la subcategoría
 * @param comments       comentarios opcionales
 * @param isSystem       true si es una subcategoría del sistema
 * @param categoryId     identificador de la categoria padre
 * @param categoryName   nombre de la categoria padre
 * @param createdById    identificador del usuario creador
 * @param createdByEmail correo del usuario creador
 * @param createdAt      fecha de creación
 * @param updatedAt      fecha de última actualización
 * @param deletedAt      fecha de eliminación lógica
 */
public record SubcategoryResponse(
        Long id,
        String name,
        String comments,
        Boolean isSystem,
        Long categoryId,
        String categoryName,
        Long createdById,
        String createdByEmail,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime deletedAt
) {
}
