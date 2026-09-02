package com.fcs.mis_fichas.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "DTO con un elemento del top de categorías o subcategorías con más gasto")
public record TopExpenseEntry(
        @Schema(description = "Identificador de la categoría o subcategoría", example = "3")
        Long id,

        @Schema(description = "Nombre de la categoría o subcategoría", example = "Alimentación")
        String name,

        @Schema(description = "Monto total gastado", example = "450.00")
        BigDecimal amount,

        @Schema(description = "Porcentaje del gasto total", example = "35.2")
        double percentage
) {
}
