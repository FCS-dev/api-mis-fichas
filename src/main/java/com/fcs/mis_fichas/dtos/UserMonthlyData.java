package com.fcs.mis_fichas.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos mensuales de usuarios para gráficos de evolución")
public record UserMonthlyData(
        @Schema(description = "Año", example = "2026")
        int year,

        @Schema(description = "Mes (1-12)", example = "6")
        int month,

        @Schema(description = "Usuarios activos en el mes (con al menos 1 transacción)", example = "80")
        long activeUsers,

        @Schema(description = "Usuarios registrados acumulados hasta el mes", example = "100")
        long registeredUsers,

        @Schema(description = "Nuevos usuarios registrados en el mes", example = "10")
        long newUsers
) {
}
