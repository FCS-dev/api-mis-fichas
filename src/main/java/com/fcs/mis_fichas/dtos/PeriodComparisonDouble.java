package com.fcs.mis_fichas.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Comparación entre período actual y anterior con porcentaje de cambio (valores decimales)")
public record PeriodComparisonDouble(
        @Schema(description = "Valor del período actual", example = "2.8")
        BigDecimal current,

        @Schema(description = "Valor del período anterior", example = "2.5")
        BigDecimal previous,

        @Schema(description = "Porcentaje de cambio (positivo = subida, negativo = bajada)", example = "12.0")
        double changePercent
) {
}
