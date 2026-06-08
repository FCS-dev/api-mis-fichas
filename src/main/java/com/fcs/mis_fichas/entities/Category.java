// Tabla de categorías de las transacciones.
// Incluye categorías propias del sistema (isSystem = TRUE) y las particulares de cada USER (isSystem = FALSE)
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

@Entity
@Table(name = "categories",
        indexes = {@Index(name = "idx_created_by", columnList = "created_by")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor // Requerido para que funcione el @Builder
@Builder
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank // valida lo que viene de la api. !null, !empty, !=" "
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false, length = 20)
    private Type type;

    @Column(nullable = false)
    private Boolean isSystem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    public Category(String name, Type type, Boolean isSystem, User createdBy, LocalDateTime deletedAt) {
        this.name = name;
        this.type = type;
        this.isSystem = isSystem;
        this.createdBy = createdBy;
        this.deletedAt = deletedAt;
    }
}
