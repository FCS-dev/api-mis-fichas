// Tabla de todas las transacciones.

package com.fcs.mis_fichas.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que representa la tabla de transacciones.
 * Cada transaccion esta asociada a un usuario, una categoria y una subcategoria.
 * Soporta soft delete mediante el campo {@code deletedAt}.
 */
@Entity
@Table(name = "transactions",
        indexes = {@Index(name = "idx_user_id", columnList = "user_id"),
                @Index(name = "idx_category_id", columnList = "category_id"),
                @Index(name = "idx_subcategory_id", columnList = "subcategory_id"),
                @Index(name = "idx_transaction_date", columnList = "transaction_date")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor // Requerido para que funcione el @Builder
@Builder
public class Transaction {

    /**
     * Identificador unico de la transaccion.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Usuario propietario de la transaccion.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @NotNull
    private User userId;

    /**
     * Categoria asociada a la transaccion.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @NotNull
    private Category category;

    /**
     * Subcategoria asociada a la transaccion.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcategory_id")
    @NotNull
    private Subcategory subcategoryId;

    /**
     * Monto de la transaccion. Debe ser un valor positivo.
     */
    @NotNull
    @Column(precision = 8, scale = 2, nullable = false)
    @Positive
    private BigDecimal amount;

    /**
     * Descripcion opcional de la transaccion.
     */
    @Column(length = 150)
    private String description;

    /**
     * Fecha en la que se realizo la transaccion.
     */
    @NotNull
    private LocalDate transactionDate;

    /**
     * Fecha y hora de creacion del registro.
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Fecha y hora de la ultima actualizacion.
     */
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Fecha y hora de eliminacion logica (soft delete).
     */
    private LocalDateTime deletedAt;

    /**
     * Constructor sin campos de auditoria ni soft delete.
     *
     * @param userId         usuario propietario
     * @param category       categoria asociada
     * @param subcategoryId  subcategoria asociada
     * @param amount         monto de la transaccion
     * @param description    descripcion opcional
     * @param transactionDate fecha de la transaccion
     */
    public Transaction(User userId, Category category, Subcategory subcategoryId, BigDecimal amount, String description, LocalDate transactionDate) {
        this.userId = userId;
        this.category = category;
        this.subcategoryId = subcategoryId;
        this.amount = amount;
        this.description = description;
        this.transactionDate = transactionDate;
    }
}
