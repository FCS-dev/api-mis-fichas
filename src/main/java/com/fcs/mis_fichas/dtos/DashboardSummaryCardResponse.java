package com.fcs.mis_fichas.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "DTO consolidado con ingresos, gastos, balance y saving rate del mes")
public record DashboardSummaryCardResponse(
        @Schema(description = "Total de ingresos del mes", example = "3000.00")
        BigDecimal income,

        @Schema(description = "Total de gastos del mes", example = "2100.00")
        BigDecimal expense,

        @Schema(description = "Balance del mes (ingresos - gastos)", example = "900.00")
        BigDecimal balance,

        @Schema(description = "Saving rate porcentual ((income-expense)/income*100)", example = "30.0")
        double savingRate
) {
}
