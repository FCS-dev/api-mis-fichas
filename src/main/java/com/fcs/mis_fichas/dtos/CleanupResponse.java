package com.fcs.mis_fichas.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Resultado de una operación de limpieza de tokens de refresco.
 */
@Schema(description = "Resultado de la limpieza de refresh tokens revocados y vencidos")
public record CleanupResponse(
        @Schema(description = "Cantidad de tokens eliminados", example = "42")
        long deletedCount
) {
}
