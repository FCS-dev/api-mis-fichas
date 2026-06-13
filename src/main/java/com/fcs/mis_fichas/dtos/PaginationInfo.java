package com.fcs.mis_fichas.dtos;

/**
 * Metadatos de paginación para respuestas de listados.
 *
 * @param currentPage   número de página actual (0-based)
 * @param pageSize      cantidad de elementos por página
 * @param totalPages    total de páginas disponibles
 * @param totalElements total de elementos en todas las páginas
 * @param first         true si es la primera pagina
 * @param last          true si es la ultima pagina
 * @param sort          criterio de ordenamiento aplicado
 */
public record PaginationInfo(
        int currentPage,
        int pageSize,
        int totalPages,
        long totalElements,
        boolean first,
        boolean last,
        String sort
) {
}
