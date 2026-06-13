package com.fcs.mis_fichas.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad que representa la tabla de subcategorías.
 * Cada subcategoría pertenece a una categoria y puede ser del sistema ({@code isSystem = true})
 * o creada por un usuario.
 * Soporta soft delete mediante el campo {@code deletedAt}.
 */
@Entity
@Table(name = "subcategories",
        indexes = {@Index(name = "idx_category_id", columnList = "category_id"),
                @Index(name = "idx_name", columnList = "name"),
                @Index(name = "idx_created_by", columnList = "created_by")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor // Requerido para que funcione el @Builder
@Builder
public class Subcategory {

    /**
     * Identificador único de la subcategoría.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Categoria a la que pertenece esta subcategoria.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    /**
     * Nombre de la subcategoría. No puede estar vacío.
     */
    @NotBlank
    @Column(length = 50, nullable = false)
    private String name;

    /**
     * Comentarios opcionales sobre la subcategoría.
     */
    @Column(length = 100)
    private String comments;

    /**
     * Indica si la subcategoría es del sistema (true) o creada por un usuario (false).
     */
    @Column(nullable = false)
    private Boolean isSystem;

    /**
     * Usuario que creo la subcategoría.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

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
     * Constructor sin el campo deletedAt.
     *
     * @param category  categoria a la que pertenece
     * @param name      nombre de la subcategoría
     * @param comments  comentarios opcionales
     * @param isSystem  true si es del sistema
     * @param createdBy usuario creador
     * @param createdAt fecha de creación
     * @param updatedAt fecha de última actualización
     */
    public Subcategory(Category category, String name, String comments, Boolean isSystem, User createdBy, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.category = category;
        this.name = name;
        this.comments = comments;
        this.isSystem = isSystem;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
