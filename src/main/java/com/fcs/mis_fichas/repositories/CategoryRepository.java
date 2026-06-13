package com.fcs.mis_fichas.repositories;

import com.fcs.mis_fichas.entities.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la gestion de categorías.
 * Proporciona operaciones de consulta con soporte para soft delete.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Busca una categoria activa por su nombre.
     *
     * @param name nombre de la categoria
     * @return Optional con la categoria encontrada o vacío si no existe o está eliminada
     */
    Optional<Category> findByNameAndDeletedAtIsNull(String name);

    /**
     * Verifica si existe una categoria activa con el nombre dado.
     *
     * @param name nombre de la categoria
     * @return true si existe una categoria activa con ese nombre
     */
    boolean existsByNameAndDeletedAtIsNull(String name);

    /**
     * Busca una categoria activa por su identificador.
     *
     * @param id identificador de la categoria
     * @return Optional con la categoria encontrada o vacío si no existe o está eliminada
     */
    Optional<Category> findByIdAndDeletedAtIsNull(Long id);

    /**
     * Busca todas las categorías activas (sin soft delete) de forma paginada.
     *
     * @param pageable información de paginación y ordenamiento
     * @return página de categorías activas
     */
    Page<Category> findByDeletedAtIsNull(Pageable pageable);
}
