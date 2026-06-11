package com.fcs.mis_fichas.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de solicitud para crear o actualizar una subcategoria.
 *
 * @param name       nombre de la subcategoria (obligatorio)
 * @param categoryId identificador de la categoria a la que pertenece (obligatorio)
 * @param comments   comentarios opcionales
 */
public record SubcategoryRequest(
        @NotBlank(message = "Name is required")
        String name,

        @NotNull(message = "Category ID is required")
        Long categoryId,

        String comments
) {
}
