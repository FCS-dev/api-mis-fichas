package com.fcs.mis_fichas.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de solicitud para crear o actualizar una subcategoría.
 */
@Schema(description = "DTO de solicitud para crear o actualizar una subcategoría")
public record SubcategoryRequest(
        @NotBlank(message = "Name is required")
        @Schema(description = "Nombre de la subcategoría", example = "Supermercado")
        String name,

        @NotNull(message = "Category ID is required")
        @Schema(description = "Identificador de la categoría a la que pertenece", example = "1")
        Long categoryId,

        @Schema(description = "Comentarios opcionales", example = "Compras semanales")
        String comments
) {
}
