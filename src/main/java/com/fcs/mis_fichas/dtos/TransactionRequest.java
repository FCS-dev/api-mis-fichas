package com.fcs.mis_fichas.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de solicitud para crear o actualizar una transacción.
 * El campo {@code userId} se ignora en la creación (se usa el usuario autenticado).
 */
@Schema(description = "DTO de solicitud para crear o actualizar una transacción")
public record TransactionRequest(
        @Schema(description = "Identificador del usuario (ignorado en POST, relevante para ADMIN en PUT)", example = "1")
        Long userId,

        @NotNull(message = "Category ID is required")
        @Schema(description = "Identificador de la categoría asociada", example = "1")
        Long categoryId,

        @NotNull(message = "Subcategory ID is required")
        @Schema(description = "Identificador de la subcategoría asociada", example = "1")
        Long subcategoryId,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        @Schema(description = "Monto de la transacción (debe ser positivo)", example = "150.50")
        BigDecimal amount,

        @Schema(description = "Descripción opcional de la transacción", example = "Compra en supermercado")
        String description,

        @NotNull(message = "Transaction date is required")
        @Schema(description = "Fecha en la que se realizó la transacción", example = "2024-01-15")
        LocalDate transactionDate
) {
}
