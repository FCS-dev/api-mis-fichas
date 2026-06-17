package com.fcs.mis_fichas.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Metadatos de paginación para respuestas de listados.
 */
@Schema(description = "Metadatos de paginación para respuestas de listados")
public record PaginationInfo(
        @Schema(description = "Número de página actual (0-based)", example = "0")
        int currentPage,

        @Schema(description = "Cantidad de elementos por página", example = "20")
        int pageSize,

        @Schema(description = "Total de páginas disponibles", example = "5")
        int totalPages,

        @Schema(description = "Total de elementos en todas las páginas", example = "100")
        long totalElements,

        @Schema(description = "true si es la primera página", example = "true")
        boolean first,

        @Schema(description = "true si es la última página", example = "false")
        boolean last,

        @Schema(description = "Criterio de ordenamiento aplicado", example = "name: ASC")
        String sort
) {
}
