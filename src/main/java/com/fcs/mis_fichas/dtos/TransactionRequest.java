package com.fcs.mis_fichas.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de solicitud para crear o actualizar una transacción.
 * El campo {@code userId} se ignora en la creación (se usa el usuario autenticado).
 *
 * @param userId          identificador del usuario (ignorado en POST, relevante para ADMIN en PUT)
 * @param categoryId      identificador de la categoria asociada
 * @param subcategoryId   identificador de la subcategoría asociada
 * @param amount          monto de la transacción (debe ser positivo)
 * @param description     descripción opcional de la transacción
 * @param transactionDate fecha en la que se realizó la transacción
 */
public record TransactionRequest(
        Long userId,

        @NotNull(message = "Category ID is required")
        Long categoryId,

        @NotNull(message = "Subcategory ID is required")
        Long subcategoryId,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        BigDecimal amount,

        String description,

        @NotNull(message = "Transaction date is required")
        LocalDate transactionDate
) {
}
