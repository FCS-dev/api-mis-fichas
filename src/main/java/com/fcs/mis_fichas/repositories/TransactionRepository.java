package com.fcs.mis_fichas.repositories;

import com.fcs.mis_fichas.entities.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Repositorio para la gestion de transacciones.
 * Proporciona operaciones de consulta con soporte para soft delete y filtros dinamicos.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    /**
     * Busca una transaccion activa (no eliminada) por su identificador.
     *
     * @param id identificador de la transaccion
     * @return Optional con la transaccion encontrada o vacio si no existe o esta eliminada
     */
    @Query("SELECT t FROM Transaction t WHERE t.deletedAt IS NULL AND t.id = :id")
    Optional<Transaction> findByIdAndDeletedAtIsNull(@Param("id") Long id);

    /**
     * Busca transacciones activas aplicando filtros dinamicos opcionales.
     * Todos los filtros son opcionales; si se pasa null, ese filtro se ignora.
     *
     * @param userId        identificador del usuario (opcional)
     * @param categoryId    identificador de la categoria (opcional)
     * @param subcategoryId identificador de la subcategoria (opcional)
     * @param date          fecha exacta de la transaccion (opcional)
     * @param dateFrom      fecha de inicio del rango (opcional)
     * @param dateTo        fecha de fin del rango (opcional)
     * @param pageable      informacion de paginacion y ordenamiento
     * @return pagina de transacciones que cumplen los filtros
     */
    @Query("SELECT t FROM Transaction t WHERE t.deletedAt IS NULL " +
            "AND (:userId IS NULL OR t.userId.id = :userId) " +
            "AND (:categoryId IS NULL OR t.category.id = :categoryId) " +
            "AND (:subcategoryId IS NULL OR t.subcategoryId.id = :subcategoryId) " +
            "AND (:date IS NULL OR t.transactionDate = :date) " +
            "AND (:dateFrom IS NULL OR t.transactionDate >= :dateFrom) " +
            "AND (:dateTo IS NULL OR t.transactionDate <= :dateTo)")
    Page<Transaction> findAllWithFilters(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("subcategoryId") Long subcategoryId,
            @Param("date") LocalDate date,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo,
            Pageable pageable);
}
