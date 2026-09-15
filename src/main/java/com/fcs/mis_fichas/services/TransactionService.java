package com.fcs.mis_fichas.services;

import com.fcs.mis_fichas.dtos.TransactionRequest;
import com.fcs.mis_fichas.dtos.TransactionResponse;
import com.fcs.mis_fichas.entities.Category;
import com.fcs.mis_fichas.entities.Subcategory;
import com.fcs.mis_fichas.entities.Transaction;
import com.fcs.mis_fichas.entities.User;
import com.fcs.mis_fichas.enums.Role;
import com.fcs.mis_fichas.repositories.CategoryRepository;
import com.fcs.mis_fichas.repositories.SubcategoryRepository;
import com.fcs.mis_fichas.repositories.TransactionRepository;
import com.fcs.mis_fichas.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Servicio de gestion de transacciones.
 * Controla las operaciones de creación, actualización, eliminación y consulta
 * con validaciones de permisos basadas en roles (USER / ADMIN).
 */
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final SubcategoryRepository subcategoryRepository;
    private final UserRepository userRepository;

    /**
     * Crea una nueva transacción asociada al usuario autenticado.
     * Los ADMIN no pueden crear transacciones propias.
     * Valida que la categoria y subcategoría existan, no estén eliminadas
     * y que la subcategoría pertenezca a la categoria indicada.
     *
     * @param request datos de la transacción a crear
     * @return DTO de respuesta con la transacción creada
     * @throws IllegalArgumentException si el usuario es ADMIN, o si la categoria/subcategoria no existe o no coincide
     */
    @Transactional
    public TransactionResponse create(TransactionRequest request) {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("ADMIN cannot register own transactions");
        }

        User targetUser = currentUser;
        Category category = categoryRepository.findByIdAndDeletedAtIsNull(request.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found or is deleted: " + request.categoryId()));
        Subcategory subcategory = subcategoryRepository.findByIdAndDeletedAtIsNull(request.subcategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Subcategory not found or is deleted: " + request.subcategoryId()));

        if (!subcategory.getCategory().getId().equals(category.getId())) {
            throw new IllegalArgumentException("Subcategory does not belong to the specified category");
        }
        checkSubcategoryAccess(subcategory, currentUser);

        Transaction transaction = Transaction.builder()
                .user(targetUser)
                .category(category)
                .subcategory(subcategory)
                .amount(request.amount())
                .description(request.description())
                .transactionDate(request.transactionDate())
                .build();

        Transaction saved = transactionRepository.save(transaction);
        return mapToResponse(saved);
    }

    /**
     * Actualiza una transacción existente.
     * USER: solo puede modificar sus propias transacciones.
     * ADMIN: puede modificar transacciones de cualquier USER, pero no de otro ADMIN.
     * Valida que la categoria y subcategoría existan y no estén eliminadas.
     *
     * @param id      identificador de la transacción a actualizar
     * @param request nuevos datos de la transacción
     * @return DTO de respuesta con la transacción actualizada
     * @throws IllegalArgumentException si la transacción no existe, no tiene permisos, o la categoria/subcategoria es inválida
     */
    @Transactional
    public TransactionResponse update(Long id, TransactionRequest request) {
        User currentUser = getCurrentUser();
        Transaction transaction = transactionRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found with id: " + id));

        if (currentUser.getRole() == Role.ADMIN) {
            if (transaction.getUser() == null || transaction.getUser().getRole() == Role.ADMIN) {
                throw new IllegalArgumentException("ADMIN can only modify transactions of USER accounts");
            }
        } else {
            if (transaction.getUser() == null || !transaction.getUser().getId().equals(currentUser.getId())) {
                throw new IllegalArgumentException("You can only modify your own transactions");
            }
        }

        Category category = categoryRepository.findByIdAndDeletedAtIsNull(request.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found or is deleted: " + request.categoryId()));
        Subcategory subcategory = subcategoryRepository.findByIdAndDeletedAtIsNull(request.subcategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Subcategory not found or is deleted: " + request.subcategoryId()));

        if (!subcategory.getCategory().getId().equals(category.getId())) {
            throw new IllegalArgumentException("Subcategory does not belong to the specified category");
        }
        checkSubcategoryAccess(subcategory, currentUser);

        transaction.setCategory(category);
        transaction.setSubcategory(subcategory);
        transaction.setAmount(request.amount());
        transaction.setDescription(request.description());
        transaction.setTransactionDate(request.transactionDate());

        Transaction saved = transactionRepository.save(transaction);
        return mapToResponse(saved);
    }

    /**
     * Elimina una transacción de forma lógica (soft delete).
     * USER: solo puede eliminar sus propias transacciones.
     * ADMIN: puede eliminar transacciones de cualquier USER, pero no de otro ADMIN.
     *
     * @param id identificador de la transacción a eliminar
     * @throws IllegalArgumentException si la transacción no existe o no tiene permisos
     */
    @Transactional
    public void delete(Long id) {
        User currentUser = getCurrentUser();
        Transaction transaction = transactionRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found with id: " + id));

        if (currentUser.getRole() == Role.ADMIN) {
            if (transaction.getUser() == null || transaction.getUser().getRole() == Role.ADMIN) {
                throw new IllegalArgumentException("ADMIN can only delete transactions of USER accounts");
            }
        } else {
            if (transaction.getUser() == null || !transaction.getUser().getId().equals(currentUser.getId())) {
                throw new IllegalArgumentException("You can only delete your own transactions");
            }
        }

        transaction.setDeletedAt(LocalDateTime.now());
        transactionRepository.save(transaction);
    }

    /**
     * Busca una transacción por su identificador.
     * USER: solo puede ver sus propias transacciones.
     * ADMIN: puede ver cualquier transacción.
     *
     * @param id identificador de la transacción
     * @return DTO de respuesta con la transacción encontrada
     * @throws IllegalArgumentException si la transacción no existe o no tiene permisos
     */
    @Transactional(readOnly = true)
    public TransactionResponse findById(Long id) {
        User currentUser = getCurrentUser();
        Transaction transaction = transactionRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found with id: " + id));

        checkViewPermission(transaction, currentUser);
        return mapToResponse(transaction);
    }

    /**
     * Busca transacciones aplicando filtros dinámicos y paginación.
     * USER: siempre filtra por su propio userId, ignorando el parámetro.
     * ADMIN: puede filtrar por cualquier usuario o ver todos si userId es null.
     *
     * @param userId        identificador de usuario a filtrar (solo para ADMIN)
     * @param categoryId    identificador de categoria a filtrar (opcional)
     * @param subcategoryId identificador de subcategoría a filtrar (opcional)
     * @param date          fecha exacta a filtrar (opcional)
     * @param dateFrom      fecha inicio de rango a filtrar (opcional)
     * @param dateTo        fecha fin de rango a filtrar (opcional)
     * @param pageable      información de paginación y ordenamiento
     * @return página de transacciones que cumplen los filtros
     */
    @Transactional(readOnly = true)
    public Page<TransactionResponse> findAll(Long userId, Long categoryId, Long subcategoryId, LocalDate date, LocalDate dateFrom, LocalDate dateTo, Pageable pageable) {
        User currentUser = getCurrentUser();
        Long effectiveUserId = null;
        if (currentUser.getRole() == Role.USER) {
            effectiveUserId = currentUser.getId();
        } else {
            effectiveUserId = userId;
        }
        return transactionRepository.findAllWithFilters(effectiveUserId, categoryId, subcategoryId, date, dateFrom, dateTo, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Verifica que el usuario tenga permiso para ver la transacción.
     * ADMIN: siempre tiene permiso.
     * USER: solo si la transacción pertenece a él.
     *
     * @param transaction transacción a verificar
     * @param currentUser usuario autenticado
     * @throws IllegalArgumentException si el usuario no tiene permisos
     */
    private void checkViewPermission(Transaction transaction, User currentUser) {
        if (currentUser.getRole() == Role.ADMIN) {
            return;
        }
        if (transaction.getUser() != null && transaction.getUser().getId().equals(currentUser.getId())) {
            return;
        }
        throw new IllegalArgumentException("You do not have permission to view this transaction");
    }

    /**
     * Verifica que el usuario tenga permiso para usar la subcategoría en una transacción.
     * ADMIN: siempre tiene permiso.
     * USER: solo si la subcategoría es del sistema o fue creada por él.
     *
     * @param subcategory subcategoría a verificar
     * @param currentUser usuario autenticado
     * @throws IllegalArgumentException si el usuario no tiene permisos
     */
    private void checkSubcategoryAccess(Subcategory subcategory, User currentUser) {
        if (currentUser.getRole() == Role.ADMIN) {
            return;
        }
        if (Boolean.TRUE.equals(subcategory.getIsSystem())) {
            return;
        }
        if (subcategory.getCreatedBy() != null && subcategory.getCreatedBy().getId().equals(currentUser.getId())) {
            return;
        }
        throw new IllegalArgumentException("You do not have permission to use this subcategory");
    }

    /**
     * Convierte una entidad Transaction a su DTO de respuesta.
     *
     * @param transaction entidad a convertir
     * @return DTO de respuesta con los datos de la transacción
     */
    private TransactionResponse mapToResponse(Transaction transaction) {
        User user = transaction.getUser();
        Category category = transaction.getCategory();
        Subcategory subcategory = transaction.getSubcategory();
        return new TransactionResponse(
                transaction.getId(),
                user != null ? user.getId() : null,
                user != null ? user.getEmail() : null,
                category != null ? category.getId() : null,
                category != null ? category.getName() : null,
                subcategory != null ? subcategory.getId() : null,
                subcategory != null ? subcategory.getName() : null,
                transaction.getAmount(),
                transaction.getDescription(),
                transaction.getTransactionDate(),
                transaction.getCreatedAt(),
                transaction.getUpdatedAt(),
                transaction.getDeletedAt()
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
