package com.fcs.mis_fichas.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "DTO con el balance mensual (ingresos - gastos) y saving rate")
public record MonthlyBalanceResponse(
        @Schema(description = "Año", example = "2026")
        int year,

        @Schema(description = "Mes (1-12)", example = "6")
        int month,

        @Schema(description = "Total de ingresos del mes", example = "2000.00")
        BigDecimal income,

        @Schema(description = "Total de gastos del mes", example = "1500.00")
        BigDecimal expense,

        @Schema(description = "Balance del mes (ingresos - gastos)", example = "500.00")
        BigDecimal balance,

        @Schema(description = "Saving rate porcentual ((income-expense)/income*100)", example = "25.0")
        double savingRate
) {
}
