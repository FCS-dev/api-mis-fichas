package com.fcs.mis_fichas.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Entrada de un usuario en el ranking de top usuarios")
public record TopUserEntry(
        @Schema(description = "ID del usuario", example = "1")
        Long userId,

        @Schema(description = "Nombre del usuario", example = "User A")
        String userName,

        @Schema(description = "Valor acumulado (cantidad o monto)", example = "184")
        BigDecimal value
) {
}
