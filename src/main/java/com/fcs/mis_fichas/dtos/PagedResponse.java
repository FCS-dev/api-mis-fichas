package com.fcs.mis_fichas.dtos;

import java.util.List;

/**
 * Wrapper para respuestas de listados paginados.
 * Contiene la lista de elementos y la información de paginación.
 *
 * @param content    lista de elementos de la pagina actual
 * @param pagination metadatos de paginación
 */
public record PagedResponse<T>(
        List<T> content,
        PaginationInfo pagination
) {
}
