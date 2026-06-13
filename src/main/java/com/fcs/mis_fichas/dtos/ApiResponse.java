package com.fcs.mis_fichas.dtos;

import java.time.LocalDateTime;

/**
 * Wrapper estándar para todas las respuestas de la API.
 * Unifica el formato de respuestas exitosas (2xx) y de error (4xx/5xx).
 *
 * @param success   true si la operación fue exitosa, false si hubo un error
 * @param status    código HTTP de la respuesta
 * @param message   mensaje descriptivo (null en éxitos, detalle del error en fallos)
 * @param data      payload de la respuesta (null en errores o cuando no aplica)
 * @param timestamp fecha y hora de la respuesta
 * @param path      ruta del endpoint que genero la respuesta
 */
public record ApiResponse<T>(
        boolean success,
        int status,
        String message,
        T data,
        LocalDateTime timestamp,
        String path
) {
}
