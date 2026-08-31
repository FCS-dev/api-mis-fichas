package com.fcs.mis_fichas.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Promedios globales y filtrados de ingresos, gastos y transacciones")
public record DashboardAveragesResponse(
        @Schema(description = "Promedio de ingresos por usuario (global: SUM income / total usuarios, excluyendo ADMIN)", example = "1500.00")
        BigDecimal globalAvgIncomePerUser,

        @Schema(description = "Promedio de gastos por usuario (global: SUM expense / total usuarios, excluyendo ADMIN)", example = "1050.00")
        BigDecimal globalAvgExpensePerUser,

        @Schema(description = "Promedio de transacciones por usuario (global: total transacciones / total usuarios, excluyendo ADMIN)", example = "23.5")
        BigDecimal globalAvgTransactionsPerUser,

        @Schema(description = "ID del usuario filtrado (null si no se filtra por usuario)")
        Long filteredUserId,

        @Schema(description = "Promedio mensual de ingresos del usuario filtrado (null si no se filtra por usuario)", example = "2000.00")
        BigDecimal filteredAvgIncome,

        @Schema(description = "Promedio mensual de gastos del usuario filtrado (null si no se filtra por usuario)", example = "1400.00")
        BigDecimal filteredAvgExpense
) {
}
