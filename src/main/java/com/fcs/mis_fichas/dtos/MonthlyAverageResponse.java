package com.fcs.mis_fichas.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "DTO con el monto promedio mensual")
public record MonthlyAverageResponse(
        @Schema(description = "Año", example = "2026")
        int year,

        @Schema(description = "Mes (1-12)", example = "6")
        int month,

        @Schema(description = "Monto promedio del mes", example = "125.50")
        BigDecimal average
) {
}
