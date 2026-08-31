package com.fcs.mis_fichas.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Top 5 usuarios por actividad, gastos e ingresos")
public record TopUsersResponse(
        @Schema(description = "Top 5 usuarios con más transacciones")
        List<TopUserEntry> topByTransactions,

        @Schema(description = "Top 5 usuarios con más gastos (sumatoria EXPENSE)")
        List<TopUserEntry> topByExpenses,

        @Schema(description = "Top 5 usuarios con más ingresos (sumatoria INCOME)")
        List<TopUserEntry> topByIncome
) {
}
