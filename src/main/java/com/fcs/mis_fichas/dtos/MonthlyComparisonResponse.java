package com.fcs.mis_fichas.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO con glosas comparativas del mes actual vs el mes anterior")
public record MonthlyComparisonResponse(
        @Schema(description = "Glosa comparativa de gastos", example = "Gastaste 12.5% menos que el mes pasado.")
        String expenseGlossary,

        @Schema(description = "Glosa comparativa de ingresos", example = "Tus ingresos aumentaron 8.2% respecto al mes anterior.")
        String incomeGlossary,

        @Schema(description = "Porcentaje de cambio en gastos (positivo = más gasto)", example = "-12.5")
        double expenseChangePercent,

        @Schema(description = "Porcentaje de cambio en ingresos (positivo = más ingreso)", example = "8.2")
        double incomeChangePercent
) {
}
