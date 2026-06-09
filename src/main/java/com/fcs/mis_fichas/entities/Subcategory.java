package com.fcs.mis_fichas.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

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
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @NotBlank
    @Column(length = 50, nullable = false)
    private String name;

    @Column(length = 100)
    private String comments;

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

    public Subcategory(Category category, String name, String comments, Boolean isSystem, User createdBy, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
        this.category = category;
        this.name = name;
        this.comments = comments;
        this.isSystem = isSystem;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }
}
