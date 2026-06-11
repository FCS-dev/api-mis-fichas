package com.fcs.mis_fichas.dtos;

import java.time.LocalDateTime;

/**
 * DTO de respuesta que representa una subcategoria.
 * Incluye informacion de la categoria padre, el creador y los datos de auditoria.
 *
 * @param id            identificador unico de la subcategoria
 * @param name          nombre de la subcategoria
 * @param comments      comentarios opcionales
 * @param isSystem      true si es una subcategoria del sistema
 * @param categoryId    identificador de la categoria padre
 * @param categoryName  nombre de la categoria padre
 * @param createdById   identificador del usuario creador
 * @param createdByEmail correo del usuario creador
 * @param createdAt     fecha de creacion
 * @param updatedAt     fecha de ultima actualizacion
 * @param deletedAt     fecha de eliminacion logica
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
