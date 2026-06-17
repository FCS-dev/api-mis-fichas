package com.fcs.mis_fichas.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Wrapper para respuestas de listados paginados.
 * Contiene la lista de elementos y la información de paginación.
 */
@Schema(description = "Wrapper para respuestas de listados paginados")
public record PagedResponse<T>(
        @Schema(description = "Lista de elementos de la página actual")
        List<T> content,

        @Schema(description = "Metadatos de paginación")
        PaginationInfo pagination
) {
}
