package com.fcs.mis_fichas.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Datos mensuales de transacciones para gráficos de evolución")
public record TransactionMonthlyData(
        @Schema(description = "Año", example = "2026")
        int year,

        @Schema(description = "Mes (1-12)", example = "6")
        int month,

        @Schema(description = "Cantidad de transacciones en el mes", example = "250")
        long transactionCount,

        @Schema(description = "Promedio de transacciones por usuario en el mes", example = "2.1")
        BigDecimal avgPerUser,

        @Schema(description = "Total de ingresos del mes", example = "5000.00")
        BigDecimal incomeTotal,

        @Schema(description = "Total de gastos del mes", example = "3200.00")
        BigDecimal expenseTotal
) {
}
