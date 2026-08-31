package com.fcs.mis_fichas.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Evolución de transacciones con resumen y datos mensuales")
public record TransactionEvolutionResponse(
        @Schema(description = "Resumen comparativo del período")
        TransactionEvolutionSummary summary,

        @Schema(description = "Datos mensuales para gráficos")
        List<TransactionMonthlyData> monthly
) {
}
