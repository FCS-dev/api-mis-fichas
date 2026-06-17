package com.fcs.mis_fichas.dtos;

import com.fcs.mis_fichas.enums.Type;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * DTO de respuesta que representa una categoria.
 * Incluye información del creador y los datos de auditoria.
 */
@Schema(description = "DTO de respuesta que representa una categoría")
public record CategoryResponse(
        @Schema(description = "Identificador único de la categoría", example = "1")
        Long id,

        @Schema(description = "Nombre de la categoría", example = "Alimentación")
        String name,

        @Schema(description = "Tipo de la categoría (INCOME o EXPENSE)", example = "EXPENSE")
        Type type,

        @Schema(description = "Identificador del usuario creador", example = "1")
        Long createdById,

        @Schema(description = "Correo del usuario creador", example = "admin@example.com")
        String createdByEmail,

        @Schema(description = "Fecha de creación", example = "2024-01-01T10:00:00")
        LocalDateTime createdAt,

        @Schema(description = "Fecha de última actualización", example = "2024-01-10T12:00:00")
        LocalDateTime updatedAt,

        @Schema(description = "Fecha de eliminación lógica (null si está activa)", example = "null")
        LocalDateTime deletedAt
) {
}
