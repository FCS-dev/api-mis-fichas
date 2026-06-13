package com.fcs.mis_fichas.services;

import com.fcs.mis_fichas.dtos.SubcategoryRequest;
import com.fcs.mis_fichas.dtos.SubcategoryResponse;
import com.fcs.mis_fichas.entities.Category;
import com.fcs.mis_fichas.entities.Subcategory;
import com.fcs.mis_fichas.entities.User;
import com.fcs.mis_fichas.enums.Role;
import com.fcs.mis_fichas.repositories.CategoryRepository;
import com.fcs.mis_fichas.repositories.SubcategoryRepository;
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
 * Servicio de gestion de subcategorías.
 * Controla las operaciones de creación, actualización, eliminación y consulta
 * con validaciones de permisos basadas en roles (USER / ADMIN).
 */
@Service
@RequiredArgsConstructor
public class SubcategoryService {

    private final SubcategoryRepository subcategoryRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    /**
     * Crea una nueva subcategoría.
     * Si el usuario es ADMIN, la subcategoría se marca como del sistema ({@code isSystem = true}).
     * Valida que la categoria exista y que no haya duplicados de nombre dentro de la misma categoria.
     *
     * @param request datos de la subcategoría a crear
     * @return DTO de respuesta con la subcategoría creada
     * @throws IllegalArgumentException si la categoria no existe o si ya existe una subcategoría con ese nombre
     */
    @Transactional
    public SubcategoryResponse create(SubcategoryRequest request) {
        User currentUser = getCurrentUser();
        Category category = categoryRepository.findByIdAndDeletedAtIsNull(request.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + request.categoryId()));

        if (subcategoryRepository.existsByNameAndCategoryIdAndDeletedAtIsNull(request.name(), category.getId())) {
            throw new IllegalArgumentException("Subcategory name already exists in this category: " + request.name());
        }

        boolean isSystem = currentUser.getRole() == Role.ADMIN;

        Subcategory subcategory = Subcategory.builder()
                .name(request.name())
                .category(category)
                .comments(request.comments())
                .isSystem(isSystem)
                .createdBy(currentUser)
                .build();

        Subcategory saved = subcategoryRepository.save(subcategory);
        return mapToResponse(saved);
    }

    /**
     * Actualiza una subcategoría existente.
     * ADMIN: puede modificar cualquier subcategoría.
     * USER: solo puede modificar sus propias subcategorías (no las del sistema).
     *
     * @param id      identificador de la subcategoría a actualizar
     * @param request nuevos datos de la subcategoría
     * @return DTO de respuesta con la subcategoría actualizada
     * @throws IllegalArgumentException si no existe, no tiene permisos, o hay duplicados
     */
    @Transactional
    public SubcategoryResponse update(Long id, SubcategoryRequest request) {
        User currentUser = getCurrentUser();
        Subcategory subcategory = subcategoryRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new IllegalArgumentException("Subcategory not found with id: " + id));

        checkModifyPermission(subcategory, currentUser);

        Category category = categoryRepository.findByIdAndDeletedAtIsNull(request.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + request.categoryId()));

        if (!subcategory.getName().equals(request.name())
                && subcategoryRepository.existsByNameAndCategoryIdAndDeletedAtIsNull(request.name(), category.getId())) {
            throw new IllegalArgumentException("Subcategory name already exists in this category: " + request.name());
        }

        subcategory.setName(request.name());
        subcategory.setCategory(category);
        subcategory.setComments(request.comments());

        Subcategory saved = subcategoryRepository.save(subcategory);
        return mapToResponse(saved);
    }

    /**
     * Elimina una subcategoría de forma lógica (soft delete).
     * ADMIN: puede eliminar cualquier subcategoría.
     * USER: solo puede eliminar sus propias subcategorías (no las del sistema).
     *
     * @param id identificador de la subcategoría a eliminar
     * @throws IllegalArgumentException si no existe o no tiene permisos
     */
    @Transactional
    public void delete(Long id) {
        User currentUser = getCurrentUser();
        Subcategory subcategory = subcategoryRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new IllegalArgumentException("Subcategory not found with id: " + id));

        checkModifyPermission(subcategory, currentUser);

        subcategory.setDeletedAt(LocalDateTime.now());
        subcategoryRepository.save(subcategory);
    }

    /**
     * Busca una subcategoría por su identificador.
     * ADMIN: puede ver cualquier subcategoría.
     * USER: solo puede ver las del sistema o las propias.
     *
     * @param id identificador de la subcategoría
     * @return DTO de respuesta con la subcategoría encontrada
     * @throws IllegalArgumentException si no existe o no tiene permisos
     */
    @Transactional(readOnly = true)
    public SubcategoryResponse findById(Long id) {
        User currentUser = getCurrentUser();
        Subcategory subcategory = subcategoryRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new IllegalArgumentException("Subcategory not found with id: " + id));

        checkViewPermission(subcategory, currentUser);
        return mapToResponse(subcategory);
    }

    /**
     * Busca todas las subcategorías activas de forma paginada.
     * ADMIN: ve todas las subcategorías activas.
     * USER: ve solo las del sistema y las propias.
     *
     * @param pageable información de paginación y ordenamiento
     * @return página de subcategorías accesibles
     */
    @Transactional(readOnly = true)
    public Page<SubcategoryResponse> findAll(Pageable pageable) {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() == Role.ADMIN) {
            return subcategoryRepository.findByDeletedAtIsNull(pageable)
                    .map(this::mapToResponse);
        } else {
            return subcategoryRepository.findByDeletedAtIsNullAndAccessibleToUser(currentUser.getId(), pageable)
                    .map(this::mapToResponse);
        }
    }

    /**
     * Verifica que el usuario tenga permiso para modificar la subcategoría.
     * ADMIN: siempre tiene permiso.
     * USER: solo si la subcategoría no es del sistema y fue creada por él.
     *
     * @param subcategory subcategoría a verificar
     * @param currentUser usuario autenticado
     * @throws IllegalArgumentException si el usuario no tiene permisos
     */
    private void checkModifyPermission(Subcategory subcategory, User currentUser) {
        if (currentUser.getRole() == Role.ADMIN) {
            return;
        }
        if (subcategory.getIsSystem()) {
            throw new IllegalArgumentException("System subcategories can only be modified by an ADMIN");
        }
        if (subcategory.getCreatedBy() == null || !subcategory.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("You can only modify your own subcategories");
        }
    }

    /**
     * Verifica que el usuario tenga permiso para ver la subcategoría.
     * ADMIN: siempre tiene permiso.
     * USER: si la subcategoría es del sistema o fue creada por él.
     *
     * @param subcategory subcategoría a verificar
     * @param currentUser usuario autenticado
     * @throws IllegalArgumentException si el usuario no tiene permisos
     */
    private void checkViewPermission(Subcategory subcategory, User currentUser) {
        if (currentUser.getRole() == Role.ADMIN) {
            return;
        }
        if (subcategory.getIsSystem()) {
            return;
        }
        if (subcategory.getCreatedBy() != null && subcategory.getCreatedBy().getId().equals(currentUser.getId())) {
            return;
        }
        throw new IllegalArgumentException("You do not have permission to view this subcategory");
    }

    /**
     * Convierte una entidad Subcategory a su DTO de respuesta.
     *
     * @param subcategory entidad a convertir
     * @return DTO de respuesta con los datos de la subcategoría
     */
    private SubcategoryResponse mapToResponse(Subcategory subcategory) {
        Category category = subcategory.getCategory();
        User createdBy = subcategory.getCreatedBy();
        return new SubcategoryResponse(
                subcategory.getId(),
                subcategory.getName(),
                subcategory.getComments(),
                subcategory.getIsSystem(),
                category != null ? category.getId() : null,
                category != null ? category.getName() : null,
                createdBy != null ? createdBy.getId() : null,
                createdBy != null ? createdBy.getEmail() : null,
                subcategory.getCreatedAt(),
                subcategory.getUpdatedAt(),
                subcategory.getDeletedAt()
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
