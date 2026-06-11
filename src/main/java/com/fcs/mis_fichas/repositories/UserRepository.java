package com.fcs.mis_fichas.repositories;

import com.fcs.mis_fichas.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la gestion de usuarios.
 * Proporciona operaciones basicas de CRUD y busqueda por correo electronico.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Busca un usuario por su correo electronico.
     *
     * @param email correo electronico del usuario
     * @return Optional con el usuario encontrado o vacio si no existe
     */
    Optional<User> findByEmail(String email);
}
