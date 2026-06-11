package com.fcs.mis_fichas.dtos;

import com.fcs.mis_fichas.enums.Type;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de solicitud para crear o actualizar una categoria.
 *
 * @param name nombre de la categoria (obligatorio)
 * @param type tipo de la categoria: INCOME o EXPENSE (obligatorio)
 */
public record CategoryRequest(
        @NotBlank(message = "Name is required")
        String name,

        @NotNull(message = "Type is required")
        Type type
) {
}
