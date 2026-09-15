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
 * Repositorio para la gestion de subcategorías.
 * Proporciona operaciones de consulta con soporte para soft delete y control de acceso por usuario.
 */
@Repository
public interface SubcategoryRepository extends JpaRepository<Subcategory, Long> {

    /**
     * Busca una subcategoría activa por su identificador.
     *
     * @param id identificador de la subcategoría
     * @return Optional con la subcategoría encontrada o vacío si no existe o está eliminada
     */
    @Query("SELECT s FROM Subcategory s WHERE s.deletedAt IS NULL AND s.id = :id")
    Optional<Subcategory> findByIdAndDeletedAtIsNull(@Param("id") Long id);

    /**
     * Busca una subcategoría activa por su nombre y la categoria a la que pertenece.
     *
     * @param name       nombre de la subcategoría
     * @param categoryId identificador de la categoria
     * @return Optional con la subcategoría encontrada o vacío si no existe o está eliminada
     */
    @Query("SELECT s FROM Subcategory s WHERE s.deletedAt IS NULL AND s.name = :name AND s.category.id = :categoryId")
    Optional<Subcategory> findByNameAndCategoryIdAndDeletedAtIsNull(@Param("name") String name, @Param("categoryId") Long categoryId);

    /**
     * Verifica si existe una subcategoría activa con el nombre dado dentro de una categoria.
     *
     * @param name       nombre de la subcategoría
     * @param categoryId identificador de la categoria
     * @return true si existe una subcategoría activa con ese nombre en la categoria
     */
    @Query("SELECT COUNT(s) > 0 FROM Subcategory s WHERE s.deletedAt IS NULL AND s.name = :name AND s.category.id = :categoryId")
    boolean existsByNameAndCategoryIdAndDeletedAtIsNull(@Param("name") String name, @Param("categoryId") Long categoryId);

    /**
     * Busca subcategorías activas accesibles para un usuario:
     * subcategorías del sistema ({@code isSystem = true}) o creadas por el usuario.
     *
     * @param userId   identificador del usuario
     * @param pageable información de paginación
     * @return página de subcategorías accesibles al usuario
     */
    @Query("SELECT s FROM Subcategory s WHERE s.deletedAt IS NULL AND (s.createdBy.id = :userId OR s.isSystem = true)")
    Page<Subcategory> findByDeletedAtIsNullAndAccessibleToUser(@Param("userId") Long userId, Pageable pageable);

    /**
     * Busca todas las subcategorías activas (sin soft delete).
     *
     * @param pageable información de paginación
     * @return página de subcategorías activas
     */
    @Query("SELECT s FROM Subcategory s WHERE s.deletedAt IS NULL")
    Page<Subcategory> findByDeletedAtIsNull(Pageable pageable);

    /**
     * Busca subcategorías activas de una categoría específica.
     *
     * @param categoryId identificador de la categoría
     * @param pageable   información de paginación
     * @return página de subcategorías activas de la categoría
     */
    @Query("SELECT s FROM Subcategory s WHERE s.deletedAt IS NULL AND s.category.id = :categoryId")
    Page<Subcategory> findByCategoryIdAndDeletedAtIsNull(@Param("categoryId") Long categoryId, Pageable pageable);

    /**
     * Busca subcategorías activas de una categoría accesibles para un usuario:
     * subcategorías del sistema ({@code isSystem = true}) o creadas por el usuario.
     *
     * @param categoryId identificador de la categoría
     * @param userId     identificador del usuario
     * @param pageable   información de paginación
     * @return página de subcategorías accesibles al usuario en esa categoría
     */
    @Query("SELECT s FROM Subcategory s WHERE s.deletedAt IS NULL AND s.category.id = :categoryId AND (s.createdBy.id = :userId OR s.isSystem = true)")
    Page<Subcategory> findByCategoryIdAndDeletedAtIsNullAndAccessibleToUser(@Param("categoryId") Long categoryId, @Param("userId") Long userId, Pageable pageable);

    /**
     * Verifica si existe alguna subcategoría activa asociada a una categoría.
     *
     * @param categoryId identificador de la categoría
     * @return true si existe una subcategoría activa de esa categoría
     */
    boolean existsByCategoryIdAndDeletedAtIsNull(Long categoryId);
}
