package com.fcs.mis_fichas.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Comparación entre período actual y anterior con porcentaje de cambio")
public record PeriodComparison(
        @Schema(description = "Valor del período actual", example = "120")
        long current,

        @Schema(description = "Valor del período anterior", example = "105")
        long previous,

        @Schema(description = "Porcentaje de cambio (positivo = subida, negativo = bajada)", example = "14.3")
        double changePercent
) {
}
