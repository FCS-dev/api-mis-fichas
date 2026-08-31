package com.fcs.mis_fichas.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Categoría de actividad de usuarios con cantidad y porcentaje")
public record ActivityCategory(
        @Schema(description = "Cantidad de usuarios en esta categoría", example = "12")
        long count,

        @Schema(description = "Porcentaje del total de usuarios", example = "12.0")
        double percentage
) {
}
