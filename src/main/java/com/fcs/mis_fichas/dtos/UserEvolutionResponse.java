package com.fcs.mis_fichas.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Evolución de usuarios con resumen y datos mensuales")
public record UserEvolutionResponse(
        @Schema(description = "Resumen comparativo del período")
        UserEvolutionSummary summary,

        @Schema(description = "Datos mensuales para gráficos")
        List<UserMonthlyData> monthly
) {
}
