package com.fcs.mis_fichas.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "DTO con el resumen de gastos por categoría")
public record CategorySummaryResponse(
        @Schema(description = "Identificador de la categoría", example = "1")
        Long categoryId,

        @Schema(description = "Nombre de la categoría", example = "Alimentación")
        String categoryName,

        @Schema(description = "Monto total gastado en la categoría", example = "450.00")
        BigDecimal total
) {
}
