package com.fcs.mis_fichas.repositories;

import com.fcs.mis_fichas.entities.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la gestion de tokens de refresco.
 * Proporciona operaciones de busqueda por hash del token y por usuario.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Busca un token de refresco por su hash.
     *
     * @param tokenHash hash SHA-256 del token
     * @return Optional con el token encontrado o vacio si no existe
     */
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /**
     * Busca el token de refresco activo (no revocado) de un usuario.
     *
     * @param userId identificador del usuario
     * @return Optional con el token activo del usuario o vacio si no tiene ninguno
     */
    Optional<RefreshToken> findByUserIdAndRevokedAtIsNull(Long userId);
}
