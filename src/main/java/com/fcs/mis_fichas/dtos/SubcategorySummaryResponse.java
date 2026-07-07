package com.fcs.mis_fichas.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "DTO con el resumen de gastos por subcategoría")
public record SubcategorySummaryResponse(
        @Schema(description = "Identificador de la subcategoría", example = "1")
        Long subcategoryId,

        @Schema(description = "Nombre de la subcategoría", example = "Supermercado")
        String subcategoryName,

        @Schema(description = "Identificador de la categoría padre", example = "1")
        Long categoryId,

        @Schema(description = "Nombre de la categoría padre", example = "Alimentación")
        String categoryName,

        @Schema(description = "Monto total gastado en la subcategoría", example = "300.00")
        BigDecimal total
) {
}
