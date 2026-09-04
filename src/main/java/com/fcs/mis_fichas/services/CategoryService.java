package com.fcs.mis_fichas.services;

import com.fcs.mis_fichas.dtos.CategoryRequest;
import com.fcs.mis_fichas.dtos.CategoryResponse;
import com.fcs.mis_fichas.entities.Category;
import com.fcs.mis_fichas.entities.User;
import com.fcs.mis_fichas.repositories.CategoryRepository;
import com.fcs.mis_fichas.repositories.TransactionRepository;
import com.fcs.mis_fichas.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Servicio de gestion de categorías.
 * Proporciona operaciones CRUD para categorías con soporte de soft delete.
 */
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    /**
     * Crea una nueva categoria.
     * Valida que no exista otra categoria activa con el mismo nombre.
     * El usuario autenticado se registra como creador.
     *
     * @param request datos de la categoria a crear
     * @return DTO de respuesta con la categoria creada
     * @throws IllegalArgumentException si ya existe una categoria con ese nombre
     */
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsByNameAndDeletedAtIsNull(request.name())) {
            throw new IllegalArgumentException("Category name already exists: " + request.name());
        }

        User currentUser = getCurrentUser();

        Category category = Category.builder()
                .name(request.name())
                .type(request.type())
                .createdBy(currentUser)
                .build();

        Category saved = categoryRepository.save(category);
        return mapToResponse(saved);
    }

    /**
     * Actualiza una categoria existente.
     * Valida que no exista otra categoria activa con el mismo nombre.
     *
     * @param id      identificador de la categoria a actualizar
     * @param request nuevos datos de la categoria
     * @return DTO de respuesta con la categoria actualizada
     * @throws IllegalArgumentException si la categoria no existe o hay duplicados
     */
    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = categoryRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + id));

        if (!category.getName().equals(request.name())
                && categoryRepository.existsByNameAndDeletedAtIsNull(request.name())) {
            throw new IllegalArgumentException("Category name already exists: " + request.name());
        }

        category.setName(request.name());
        category.setType(request.type());

        Category saved = categoryRepository.save(category);
        return mapToResponse(saved);
    }

    /**
     * Elimina una categoria de forma lógica (soft delete).
     *
     * @param id identificador de la categoria a eliminar
     * @throws IllegalArgumentException si la categoria no existe o ya está eliminada
     */
    @Transactional
    public void delete(Long id) {
        Category category = categoryRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + id));

        if (transactionRepository.existsByCategoryIdAndDeletedAtIsNull(id)) {
            throw new IllegalArgumentException("No se puede eliminar: la categoría tiene transacciones activas");
        }

        category.setDeletedAt(LocalDateTime.now());
        categoryRepository.save(category);
    }

    /**
     * Busca una categoria activa por su identificador.
     *
     * @param id identificador de la categoria
     * @return DTO de respuesta con la categoria encontrada
     * @throws IllegalArgumentException si la categoria no existe o está eliminada
     */
    @Transactional(readOnly = true)
    public CategoryResponse findById(Long id) {
        Category category = categoryRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + id));
        return mapToResponse(category);
    }

    /**
     * Busca todas las categorías activas de forma paginada.
     *
     * @param pageable información de paginación y ordenamiento
     * @return página de categorías activas
     */
    @Transactional(readOnly = true)
    public Page<CategoryResponse> findAll(Pageable pageable) {
        return categoryRepository.findByDeletedAtIsNull(pageable)
                .map(this::mapToResponse);
    }

    /**
     * Convierte una entidad Category a su DTO de respuesta.
     *
     * @param category entidad a convertir
     * @return DTO de respuesta con los datos de la categoria
     */
    private CategoryResponse mapToResponse(Category category) {
        User createdBy = category.getCreatedBy();
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getType(),
                createdBy != null ? createdBy.getId() : null,
                createdBy != null ? createdBy.getEmail() : null,
                category.getCreatedAt(),
                category.getUpdatedAt(),
                category.getDeletedAt()
        );
    }

    /**
     * Obtiene el usuario autenticado desde el contexto de seguridad.
     *
     * @return entidad User del usuario autenticado
     * @throws IllegalStateException si el usuario autenticado no se encuentra en la base de datos
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database"));
    }
}
