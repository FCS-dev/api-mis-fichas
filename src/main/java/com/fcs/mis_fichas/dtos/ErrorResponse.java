package com.fcs.mis_fichas.dtos;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para errores de la API.
 * Proporciona información estructurada sobre el error ocurrido.
 *
 * @param timestamp fecha y hora en que ocurrió el error
 * @param status    código HTTP del error
 * @param error     tipo de error (ej: "Bad Request", "Unauthorized")
 * @param message   mensaje descriptivo del error
 * @param path      ruta del endpoint donde ocurrió el error
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
