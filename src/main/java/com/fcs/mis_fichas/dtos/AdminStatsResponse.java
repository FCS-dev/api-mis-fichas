package com.fcs.mis_fichas.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO con estadísticas generales para el ADMIN")
public record AdminStatsResponse(
        @Schema(description = "Cantidad total de usuarios activos", example = "150")
        long totalUsers,

        @Schema(description = "Cantidad total de transacciones registradas", example = "3500")
        long totalTransactions
) {
}
