package com.fcs.mis_fichas.repositories;

import com.fcs.mis_fichas.entities.Subcategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la gestion de subcategorias.
 * Proporciona operaciones de consulta con soporte para soft delete y control de acceso por usuario.
 */
@Repository
public interface SubcategoryRepository extends JpaRepository<Subcategory, Long> {

    /**
     * Busca una subcategoria activa por su identificador.
     *
     * @param id identificador de la subcategoria
     * @return Optional con la subcategoria encontrada o vacio si no existe o esta eliminada
     */
    @Query("SELECT s FROM Subcategory s WHERE s.deletedAt IS NULL AND s.id = :id")
    Optional<Subcategory> findByIdAndDeletedAtIsNull(@Param("id") Long id);

    /**
     * Busca una subcategoria activa por su nombre y la categoria a la que pertenece.
     *
     * @param name       nombre de la subcategoria
     * @param categoryId identificador de la categoria
     * @return Optional con la subcategoria encontrada o vacio si no existe o esta eliminada
     */
    @Query("SELECT s FROM Subcategory s WHERE s.deletedAt IS NULL AND s.name = :name AND s.category.id = :categoryId")
    Optional<Subcategory> findByNameAndCategoryIdAndDeletedAtIsNull(@Param("name") String name, @Param("categoryId") Long categoryId);

    /**
     * Verifica si existe una subcategoria activa con el nombre dado dentro de una categoria.
     *
     * @param name       nombre de la subcategoria
     * @param categoryId identificador de la categoria
     * @return true si existe una subcategoria activa con ese nombre en la categoria
     */
    @Query("SELECT COUNT(s) > 0 FROM Subcategory s WHERE s.deletedAt IS NULL AND s.name = :name AND s.category.id = :categoryId")
    boolean existsByNameAndCategoryIdAndDeletedAtIsNull(@Param("name") String name, @Param("categoryId") Long categoryId);

    /**
     * Busca subcategorias activas accesibles para un usuario:
     * subcategorias del sistema ({@code isSystem = true}) o creadas por el usuario.
     *
     * @param userId   identificador del usuario
     * @param pageable informacion de paginacion
     * @return pagina de subcategorias accesibles al usuario
     */
    @Query("SELECT s FROM Subcategory s WHERE s.deletedAt IS NULL AND (s.createdBy.id = :userId OR s.isSystem = true)")
    Page<Subcategory> findByDeletedAtIsNullAndAccessibleToUser(@Param("userId") Long userId, Pageable pageable);

    /**
     * Busca todas las subcategorias activas (sin soft delete).
     *
     * @param pageable informacion de paginacion
     * @return pagina de subcategorias activas
     */
    @Query("SELECT s FROM Subcategory s WHERE s.deletedAt IS NULL")
    Page<Subcategory> findByDeletedAtIsNull(Pageable pageable);
}
