package com.fcs.mis_fichas.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Resumen de evolución de transacciones con comparación de períodos")
public record TransactionEvolutionSummary(
        @Schema(description = "Transacciones por mes (valor promedio del período)")
        PeriodComparisonDouble transactionsPerMonth,

        @Schema(description = "Promedio de transacciones por usuario")
        PeriodComparisonDouble avgPerUser
) {
}
