package com.fcs.mis_fichas.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Distribución de usuarios por nivel de actividad en un mes")
public record ActivityDistributionResponse(
        @Schema(description = "Frecuente: más de 20 transacciones/mes")
        ActivityCategory frecuente,

        @Schema(description = "Regular: 5–20 transacciones/mes")
        ActivityCategory regular,

        @Schema(description = "Ocasional: 1–4 transacciones/mes")
        ActivityCategory ocasional,

        @Schema(description = "Inactivo: 0 transacciones/mes")
        ActivityCategory inactivo
) {
}
