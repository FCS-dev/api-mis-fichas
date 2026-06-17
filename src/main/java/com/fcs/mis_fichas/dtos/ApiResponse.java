package com.fcs.mis_fichas.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Wrapper estándar para todas las respuestas de la API.
 * Unifica el formato de respuestas exitosas (2xx) y de error (4xx/5xx).
 */
@Schema(description = "Wrapper estándar para todas las respuestas de la API")
public record ApiResponse<T>(
        @Schema(description = "true si la operación fue exitosa, false si hubo un error", example = "true")
        boolean success,

        @Schema(description = "Código HTTP de la respuesta", example = "200")
        int status,

        @Schema(description = "Mensaje descriptivo (null en éxitos, detalle del error en fallos)", example = "User registered successfully")
        String message,

        @Schema(description = "Payload de la respuesta (null en errores o cuando no aplica)")
        T data,

        @Schema(description = "Fecha y hora de la respuesta", example = "2024-01-15T10:30:00")
        LocalDateTime timestamp,

        @Schema(description = "Ruta del endpoint que generó la respuesta", example = "/api/v1/auth/register")
        String path
) {
}
