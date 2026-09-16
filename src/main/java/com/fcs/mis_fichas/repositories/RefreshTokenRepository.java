package com.fcs.mis_fichas.repositories;

import com.fcs.mis_fichas.entities.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestion de tokens de refresco.
 * Proporciona operaciones de búsqueda por hash del token y por usuario.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Busca un token de refresco por su hash.
     *
     * @param tokenHash hash SHA-256 del token
     * @return Optional con el token encontrado o vacío si no existe
     */
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /**
     * Busca todos los tokens de refresco activos (no revocados) de un usuario.
     *
     * @param userId identificador del usuario
     * @return lista de tokens activos del usuario
     */
    List<RefreshToken> findByUserIdAndRevokedAtIsNull(Long userId);

    /**
     * Elimina todos los tokens de refresco que hayan sido revocados y cuya
     * fecha de expiración ya haya pasado.
     *
     * @param now fecha y hora actual
     * @return cantidad de registros eliminados
     */
    long deleteByRevokedAtIsNotNullAndExpiresAtBefore(LocalDateTime now);
}
