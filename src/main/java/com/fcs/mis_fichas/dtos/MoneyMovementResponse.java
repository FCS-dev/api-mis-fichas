package com.fcs.mis_fichas.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Resumen de movimientos de dinero (ingresos, gastos y balance)")
public record MoneyMovementResponse(
        @Schema(description = "Total de ingresos", example = "45000.00")
        BigDecimal totalIncome,

        @Schema(description = "Total de gastos", example = "32000.00")
        BigDecimal totalExpense,

        @Schema(description = "Balance total (ingresos - gastos)", example = "13000.00")
        BigDecimal totalBalance,

        @Schema(description = "Ingresos del mes actual")
        BigDecimal currentMonthIncome,

        @Schema(description = "Ingresos del mes anterior")
        BigDecimal previousMonthIncome,

        @Schema(description = "Gastos del mes actual")
        BigDecimal currentMonthExpense,

        @Schema(description = "Gastos del mes anterior")
        BigDecimal previousMonthExpense,

        @Schema(description = "Balance del mes actual")
        BigDecimal currentMonthBalance,

        @Schema(description = "Balance del mes anterior")
        BigDecimal previousMonthBalance
) {
}
