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
 * Cada transacción está asociada a un usuario, una categoria y una subcategoría.
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
     * Identificador único de la transacción.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Usuario propietario de la transacción.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @NotNull
    private User user;

    /**
     * Categoria asociada a la transaccion.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @NotNull
    private Category category;

    /**
     * Subcategoría asociada a la transaccion.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcategory_id")
    @NotNull
    private Subcategory subcategory;

    /**
     * Monto de la transacción. Debe ser un valor positivo.
     */
    @NotNull
    @Column(precision = 8, scale = 2, nullable = false)
    @Positive
    private BigDecimal amount;

    /**
     * Descripción opcional de la transacción.
     */
    @Column(length = 150)
    private String description;

    /**
     * Fecha en la que se realizó la transacción.
     */
    @NotNull
    private LocalDate transactionDate;

    /**
     * Fecha y hora de creación del registro.
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Fecha y hora de la última actualización.
     */
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Fecha y hora de eliminación lógica (soft delete).
     */
    private LocalDateTime deletedAt;

    /**
     * Constructor sin campos de auditoria ni soft delete.
     *
     * @param user          usuario propietario
     * @param category      categoria asociada
     * @param subcategory   subcategoría asociada
     * @param amount        monto de la transacción
     * @param description   descripción opcional
     * @param transactionDate fecha de la transacción
     */
    public Transaction(User user, Category category, Subcategory subcategory, BigDecimal amount, String description, LocalDate transactionDate) {
        this.user = user;
        this.category = category;
        this.subcategory = subcategory;
        this.amount = amount;
        this.description = description;
        this.transactionDate = transactionDate;
    }
}
