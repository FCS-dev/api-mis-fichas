// Tabla para gestionar los refresh_tokens (validez 30d).

package com.fcs.mis_fichas.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad que representa la tabla de tokens de refresco (refresh tokens).
 * Cada token esta asociado a un usuario y tiene una fecha de expiracion.
 * Los tokens pueden ser revocados antes de su expiracion.
 */
@Entity
@Table(name = "refresh_tokens",
        indexes = {@Index(name = "idx_user_id", columnList = "user_id")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor // Requerido para que funcione el @Builder
@Builder
public class RefreshToken {

    /**
     * Identificador unico del token.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Usuario al que pertenece el token.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User userId;

    /**
     * Hash SHA-256 del token. Se almacena el hash en lugar del valor original.
     */
    @NotNull
    @Column(unique = true, nullable = false)
    private String tokenHash;

    /**
     * Fecha y hora de expiracion del token.
     */
    @NotNull
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Fecha y hora de revocacion. Si es null, el token aun es valido.
     */
    private LocalDateTime revokedAt;

    /**
     * Fecha y hora de creacion del registro.
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Constructor sin campos de auditoria.
     *
     * @param userId    usuario propietario
     * @param tokenHash hash del token
     * @param expiresAt fecha de expiracion
     */
    public RefreshToken(User userId, String tokenHash, LocalDateTime expiresAt) {
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }
}
