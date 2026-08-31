package com.fcs.mis_fichas.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resumen de evolución de usuarios con comparación de períodos")
public record UserEvolutionSummary(
        @Schema(description = "Usuarios activos (con transacciones en el mes)")
        PeriodComparison activeUsers,

        @Schema(description = "Usuarios registrados (acumulados)")
        PeriodComparison registeredUsers,

        @Schema(description = "Nuevos usuarios registrados en el mes")
        PeriodComparison newUsers
) {
}
