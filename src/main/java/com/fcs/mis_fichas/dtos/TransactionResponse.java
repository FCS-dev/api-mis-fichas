package com.fcs.mis_fichas.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de respuesta que representa una transacción.
 * Incluye información del usuario, categoria, subcategoría y auditoria.
 */
@Schema(description = "DTO de respuesta que representa una transacción")
public record TransactionResponse(
        @Schema(description = "Identificador único de la transacción", example = "1")
        Long id,

        @Schema(description = "Identificador del usuario propietario", example = "1")
        Long userId,

        @Schema(description = "Correo del usuario propietario", example = "user@example.com")
        String userEmail,

        @Schema(description = "Identificador de la categoría", example = "1")
        Long categoryId,

        @Schema(description = "Nombre de la categoría", example = "Alimentación")
        String categoryName,

        @Schema(description = "Identificador de la subcategoría", example = "1")
        Long subcategoryId,

        @Schema(description = "Nombre de la subcategoría", example = "Supermercado")
        String subcategoryName,

        @Schema(description = "Monto de la transacción", example = "150.50")
        BigDecimal amount,

        @Schema(description = "Descripción de la transacción", example = "Compra en supermercado")
        String description,

        @Schema(description = "Fecha de la transacción", example = "2024-01-15")
        LocalDate transactionDate,

        @Schema(description = "Fecha de creación del registro", example = "2024-01-15T10:00:00")
        LocalDateTime createdAt,

        @Schema(description = "Fecha de última actualización", example = "2024-01-15T10:00:00")
        LocalDateTime updatedAt,

        @Schema(description = "Fecha de eliminación lógica (null si está activa)", example = "null")
        LocalDateTime deletedAt
) {
}
