package com.fcs.mis_fichas.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de solicitud para crear o actualizar una transaccion.
 * El campo {@code userId} se ignora en la creacion (se usa el usuario autenticado).
 *
 * @param userId         identificador del usuario (ignorado en POST, relevante para ADMIN en PUT)
 * @param categoryId     identificador de la categoria asociada
 * @param subcategoryId  identificador de la subcategoria asociada
 * @param amount         monto de la transaccion (debe ser positivo)
 * @param description    descripcion opcional de la transaccion
 * @param transactionDate fecha en la que se realizo la transaccion
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
