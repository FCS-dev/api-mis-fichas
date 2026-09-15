//Tabla de usuarios

package com.fcs.mis_fichas.entities;

import com.fcs.mis_fichas.enums.Role;
import com.fcs.mis_fichas.enums.Status;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad que representa la tabla de usuarios del sistema.
 * Cada usuario puede tener rol USER o ADMIN, y un estado ACTIVE o BLOCKED.
 * La entidad soporta soft delete mediante el campo {@code deletedAt}.
 */
@Entity
@Table(name = "users",
        indexes = {@Index(name = "idx_name", columnList = "name")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor // Requerido para que funcione el @Builder
@Builder
public class User {

    /**
     * Identificador único del usuario.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Correo electrónico del usuario. Debe ser único y no puede estar vacío.
     */
    @NotBlank // valida lo que viene de la api. !null, !empty, !=" "
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    /**
     * Hash de la contraseña del usuario. Nunca se almacena en texto plano.
     */
    @Column(nullable = false)
    private String passwordHash;

    /**
     * Nombre completo del usuario. No puede estar vacío.
     */
    @NotBlank // valida lo que viene de la api. !null, !empty, !=" "
    @Column(nullable = false, length = 150)
    private String name;

    /**
     * Rol del usuario en el sistema (USER o ADMIN).
     */
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false, length = 20)
    private Role role;

    /**
     * Estado actual de la cuenta (ACTIVE o BLOCKED). Por defecto ACTIVE.
     */
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status = Status.ACTIVE;

    /**
     * Fecha y hora de creación del registro. Se genera automaticamente.
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Fecha y hora de la última actualización del registro. Se actualiza automaticamente.
     */
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Fecha y hora de eliminación lógica (soft delete). Si es null, el registro está activo.
     */
    private LocalDateTime deletedAt;

    /**
     * Constructor sin campos de auditoria ni builder.
     *
     * @param email        correo electrónico del usuario
     * @param passwordHash hash de la contraseña
     * @param name         nombre del usuario
     * @param role         rol del usuario
     * @param deletedAt    fecha de eliminación lógica (puede ser null)
     */
    public User(String email, String passwordHash, String name, Role role, LocalDateTime deletedAt) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.name = name;
        this.role = role;
        this.deletedAt = deletedAt;
    }
}
