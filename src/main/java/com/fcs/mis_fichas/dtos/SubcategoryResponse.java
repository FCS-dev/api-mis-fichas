package com.fcs.mis_fichas.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * DTO de respuesta que representa una subcategoría.
 * Incluye información de la categoria padre, el creador y los datos de auditoria.
 */
@Schema(description = "DTO de respuesta que representa una subcategoría")
public record SubcategoryResponse(
        @Schema(description = "Identificador único de la subcategoría", example = "1")
        Long id,

        @Schema(description = "Nombre de la subcategoría", example = "Supermercado")
        String name,

        @Schema(description = "Comentarios opcionales", example = "Compras semanales")
        String comments,

        @Schema(description = "true si es una subcategoría del sistema", example = "false")
        Boolean isSystem,

        @Schema(description = "Identificador de la categoría padre", example = "1")
        Long categoryId,

        @Schema(description = "Nombre de la categoría padre", example = "Alimentación")
        String categoryName,

        @Schema(description = "Identificador del usuario creador", example = "1")
        Long createdById,

        @Schema(description = "Correo del usuario creador", example = "user@example.com")
        String createdByEmail,

        @Schema(description = "Fecha de creación", example = "2024-01-01T10:00:00")
        LocalDateTime createdAt,

        @Schema(description = "Fecha de última actualización", example = "2024-01-10T12:00:00")
        LocalDateTime updatedAt,

        @Schema(description = "Fecha de eliminación lógica (null si está activa)", example = "null")
        LocalDateTime deletedAt
) {
}
