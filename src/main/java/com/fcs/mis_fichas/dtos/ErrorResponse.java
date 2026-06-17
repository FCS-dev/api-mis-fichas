package com.fcs.mis_fichas.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para errores de la API.
 * Proporciona información estructurada sobre el error ocurrido.
 */
@Schema(description = "DTO de respuesta para errores de la API")
public record ErrorResponse(
        @Schema(description = "Fecha y hora en que ocurrió el error", example = "2024-01-15T10:30:00")
        LocalDateTime timestamp,

        @Schema(description = "Código HTTP del error", example = "400")
        int status,

        @Schema(description = "Tipo de error", example = "Bad Request")
        String error,

        @Schema(description = "Mensaje descriptivo del error", example = "Validation failed")
        String message,

        @Schema(description = "Ruta del endpoint donde ocurrió el error", example = "/api/v1/auth/register")
        String path
) {
}
