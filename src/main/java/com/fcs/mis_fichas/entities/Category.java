// Tabla de categorias de las transacciones.
// Incluye categorias propias del sistema (isSystem = TRUE) y las particulares de cada USER (isSystem = FALSE)
// Todas deben estar clasificadas entre: INCOME, EXPENSE

package com.fcs.mis_fichas.entities;

import com.fcs.mis_fichas.enums.Type;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad que representa la tabla de categorías de transacciones.
 * Las categorías pueden ser de tipo INCOME o EXPENSE.
 * Soporta soft delete mediante el campo {@code deletedAt}.
 */
@Entity
@Table(name = "categories",
        indexes = {@Index(name = "idx_created_by", columnList = "created_by")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor // Requerido para que funcione el @Builder
@Builder
public class Category {

    /**
     * Identificador único de la categoria.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre de la categoria. No puede estar vacío.
     */
    @NotBlank // valida lo que viene de la api. !null, !empty, !=" "
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Tipo de la categoria (INCOME o EXPENSE).
     */
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false, length = 20)
    private Type type;

    /**
     * Usuario que creo la categoria. Puede ser null si es del sistema.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    /**
     * Fecha y hora de creación del registro. Se genera automaticamente.
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Fecha y hora de la ultima actualización del registro.
     */
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Fecha y hora de eliminación lógica (soft delete). Si es null, el registro está activo.
     */
    private LocalDateTime deletedAt;

    /**
     * Constructor sin campos de auditoria.
     *
     * @param name      nombre de la categoria
     * @param type      tipo de la categoria (INCOME o EXPENSE)
     * @param createdBy usuario creador de la categoria
     */
    public Category(String name, Type type, User createdBy) {
        this.name = name;
        this.type = type;
        this.createdBy = createdBy;
    }
}
