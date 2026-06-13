package com.fcs.mis_fichas.repositories;

import com.fcs.mis_fichas.entities.User;
import com.fcs.mis_fichas.enums.Role;
import com.fcs.mis_fichas.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la gestion de usuarios.
 * Proporciona operaciones básicas de CRUD, búsqueda por correo electrónico,
 * paginación con filtros por rol y estado, y validación de email único.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Busca un usuario activo (no eliminado) por su correo electrónico.
     *
     * @param email correo electrónico del usuario
     * @return Optional con el usuario encontrado o vacío si no existe
     */
    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    /**
     * Busca un usuario activo (no eliminado) por su identificador.
     *
     * @param id identificador del usuario
     * @return Optional con el usuario encontrado o vacío si no existe
     */
    Optional<User> findByIdAndDeletedAtIsNull(Long id);

    /**
     * Lista todos los usuarios activos de forma paginada.
     *
     * @param pageable información de paginación y ordenamiento
     * @return página de usuarios activos
     */
    Page<User> findByDeletedAtIsNull(Pageable pageable);

    /**
     * Lista usuarios activos filtrados por rol.
     *
     * @param role     rol a filtrar
     * @param pageable información de paginación y ordenamiento
     * @return página de usuarios activos con el rol especificado
     */
    Page<User> findByDeletedAtIsNullAndRole(Role role, Pageable pageable);

    /**
     * Lista usuarios activos filtrados por estado.
     *
     * @param status   estado a filtrar
     * @param pageable información de paginación y ordenamiento
     * @return página de usuarios activos con el estado especificado
     */
    Page<User> findByDeletedAtIsNullAndStatus(Status status, Pageable pageable);

    /**
     * Lista usuarios activos filtrados por rol y estado.
     *
     * @param role     rol a filtrar
     * @param status   estado a filtrar
     * @param pageable información de paginación y ordenamiento
     * @return página de usuarios activos con el rol y estado especificados
     */
    Page<User> findByDeletedAtIsNullAndRoleAndStatus(Role role, Status status, Pageable pageable);

    /**
     * Verifica si existe un usuario activo con el email dado.
     *
     * @param email correo electrónico a verificar
     * @return true si existe un usuario activo con ese email
     */
    boolean existsByEmailAndDeletedAtIsNull(String email);

    /**
     * Verifica si existe otro usuario activo con el email dado, excluyendo un ID.
     * Útil para validaciones de unicidad al actualizar.
     *
     * @param email correo electrónico a verificar
     * @param id    identificador del usuario a excluir
     * @return true si existe otro usuario activo con ese email
     */
    boolean existsByEmailAndDeletedAtIsNullAndIdNot(String email, Long id);
}
