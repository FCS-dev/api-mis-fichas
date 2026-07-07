package com.fcs.mis_fichas.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "DTO con el total de ingresos o gastos")
public record DashboardTotalResponse(
        @Schema(description = "Monto total", example = "1500.00")
        BigDecimal total
) {
}
