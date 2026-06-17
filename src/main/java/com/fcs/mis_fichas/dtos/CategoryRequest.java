package com.fcs.mis_fichas.dtos;

import com.fcs.mis_fichas.enums.Type;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de solicitud para crear o actualizar una categoria.
 */
@Schema(description = "DTO de solicitud para crear o actualizar una categoría")
public record CategoryRequest(
        @NotBlank(message = "Name is required")
        @Schema(description = "Nombre de la categoría", example = "Alimentación")
        String name,

        @NotNull(message = "Type is required")
        @Schema(description = "Tipo de la categoría: INCOME o EXPENSE", example = "EXPENSE")
        Type type
) {
}
