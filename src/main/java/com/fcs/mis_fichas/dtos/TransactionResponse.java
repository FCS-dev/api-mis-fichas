package com.fcs.mis_fichas.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de respuesta que representa una transacción.
 * Incluye información del usuario, categoria, subcategoría y auditoria.
 *
 * @param id              identificador único de la transacción
 * @param userId          identificador del usuario propietario
 * @param userEmail       correo del usuario propietario
 * @param categoryId      identificador de la categoria
 * @param categoryName    nombre de la categoria
 * @param subcategoryId   identificador de la subcategoría
 * @param subcategoryName nombre de la subcategoría
 * @param amount          monto de la transacción
 * @param description     descripción de la transacción
 * @param transactionDate fecha de la transacción
 * @param createdAt       fecha de creación del registro
 * @param updatedAt       fecha de última actualización
 * @param deletedAt       fecha de eliminación lógica (null si está activa)
 */
public record TransactionResponse(
        Long id,
        Long userId,
        String userEmail,
        Long categoryId,
        String categoryName,
        Long subcategoryId,
        String subcategoryName,
        BigDecimal amount,
        String description,
        LocalDate transactionDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime deletedAt
) {
}
