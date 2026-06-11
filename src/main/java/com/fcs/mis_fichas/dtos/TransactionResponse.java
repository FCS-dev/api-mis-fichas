package com.fcs.mis_fichas.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de respuesta que representa una transaccion.
 * Incluye informacion del usuario, categoria, subcategoria y auditoria.
 *
 * @param id              identificador unico de la transaccion
 * @param userId          identificador del usuario propietario
 * @param userEmail       correo del usuario propietario
 * @param categoryId      identificador de la categoria
 * @param categoryName    nombre de la categoria
 * @param subcategoryId   identificador de la subcategoria
 * @param subcategoryName nombre de la subcategoria
 * @param amount          monto de la transaccion
 * @param description     descripcion de la transaccion
 * @param transactionDate fecha de la transaccion
 * @param createdAt       fecha de creacion del registro
 * @param updatedAt       fecha de ultima actualizacion
 * @param deletedAt       fecha de eliminacion logica (null si esta activa)
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
