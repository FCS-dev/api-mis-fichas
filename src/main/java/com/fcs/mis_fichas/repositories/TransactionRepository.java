package com.fcs.mis_fichas.repositories;

import com.fcs.mis_fichas.entities.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fcs.mis_fichas.enums.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestion de transacciones.
 * Proporciona operaciones de consulta con soporte para soft delete y filtros dinámicos.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    /**
     * Busca una transacción activa (no eliminada) por su identificador.
     *
     * @param id identificador de la transacción
     * @return Optional con la transacción encontrada o vacío si no existe o está eliminada
     */
    @Query("SELECT t FROM Transaction t WHERE t.deletedAt IS NULL AND t.id = :id")
    Optional<Transaction> findByIdAndDeletedAtIsNull(@Param("id") Long id);

    /**
     * Busca transacciones activas aplicando filtros dinámicos opcionales.
     * Todos los filtros son opcionales; si se pasa null, ese filtro se ignora.
     *
     * @param userId        identificador del usuario (opcional)
     * @param categoryId    identificador de la categoria (opcional)
     * @param subcategoryId identificador de la subcategoría (opcional)
     * @param date          fecha exacta de la transacción (opcional)
     * @param dateFrom      fecha de inicio del rango (opcional)
     * @param dateTo        fecha de fin del rango (opcional)
     * @param pageable      información de paginación y ordenamiento
     * @return página de transacciones que cumplen los filtros
     */
    @Query("SELECT t FROM Transaction t WHERE t.deletedAt IS NULL " +
            "AND (:userId IS NULL OR t.user.id = :userId) " +
            "AND (:categoryId IS NULL OR t.category.id = :categoryId) " +
            "AND (:subcategoryId IS NULL OR t.subcategory.id = :subcategoryId) " +
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

    /**
     * Suma los montos de transacciones de un tipo específico para un usuario en un rango de fechas.
     *
     * @param userId identificador del usuario
     * @param type   tipo de transacción (INCOME o EXPENSE)
     * @param start  fecha de inicio del rango
     * @param end    fecha de fin del rango
     * @return suma total del monto, o 0 si no hay transacciones
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.deletedAt IS NULL " +
            "AND t.user.id = :userId AND t.category.type = :type " +
            "AND t.transactionDate BETWEEN :start AND :end")
    BigDecimal sumByUserAndTypeBetweenDates(
            @Param("userId") Long userId,
            @Param("type") Type type,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    /**
     * Agrupa y suma los gastos (EXPENSE) por categoría, con filtro opcional de usuario y rango de fechas.
     *
     * @param userId identificador del usuario (null para incluir todos)
     * @param start  fecha de inicio del rango
     * @param end    fecha de fin del rango
     * @return lista de arreglos: [categoryId, categoryName, total]
     */
    @Query("SELECT t.category.id, t.category.name, COALESCE(SUM(t.amount), 0) " +
            "FROM Transaction t WHERE t.deletedAt IS NULL " +
            "AND (:userId IS NULL OR t.user.id = :userId) " +
            "AND t.category.type = 'EXPENSE' " +
            "AND t.transactionDate BETWEEN :start AND :end " +
            "GROUP BY t.category.id, t.category.name")
    List<Object[]> expenseSumGroupedByCategory(
            @Param("userId") Long userId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    /**
     * Agrupa y suma los gastos (EXPENSE) por subcategoría dentro de una categoría específica,
     * con filtro opcional de usuario y rango de fechas.
     *
     * @param userId     identificador del usuario (null para incluir todos)
     * @param categoryId identificador de la categoría a filtrar
     * @param start      fecha de inicio del rango
     * @param end        fecha de fin del rango
     * @return lista de arreglos: [subcategoryId, subcategoryName, categoryId, categoryName, total]
     */
    @Query("SELECT t.subcategory.id, t.subcategory.name, t.category.id, t.category.name, COALESCE(SUM(t.amount), 0) " +
            "FROM Transaction t WHERE t.deletedAt IS NULL " +
            "AND (:userId IS NULL OR t.user.id = :userId) " +
            "AND t.category.type = 'EXPENSE' " +
            "AND t.category.id = :categoryId " +
            "AND t.transactionDate BETWEEN :start AND :end " +
            "GROUP BY t.subcategory.id, t.subcategory.name, t.category.id, t.category.name")
    List<Object[]> expenseSumGroupedBySubcategory(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    /**
     * Obtiene todas las transacciones activas desde una fecha en adelante,
     * con filtro opcional de usuario. Incluye la categoría (JOIN FETCH) para evitar N+1.
     *
     * @param userId    identificador del usuario (null para incluir todos)
     * @param sinceDate fecha a partir de la cual incluir transacciones
     * @return lista de transacciones
     */
    @Query("SELECT t FROM Transaction t JOIN FETCH t.category WHERE t.deletedAt IS NULL " +
            "AND (:userId IS NULL OR t.user.id = :userId) " +
            "AND t.transactionDate >= :sinceDate")
    List<Transaction> findTransactionsSince(
            @Param("userId") Long userId,
            @Param("sinceDate") LocalDate sinceDate);
}
