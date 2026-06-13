package com.fcs.mis_fichas.services;

import com.fcs.mis_fichas.dtos.UserResponse;
import com.fcs.mis_fichas.dtos.UserUpdateRequest;
import com.fcs.mis_fichas.entities.User;
import com.fcs.mis_fichas.enums.Role;
import com.fcs.mis_fichas.enums.Status;
import com.fcs.mis_fichas.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Servicio de gestión de usuarios.
 * Proporciona operaciones CRUD y consultas para el rol ADMIN.
 * Soporta soft delete, paginación y filtros por rol y estado.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Busca un usuario activo por su identificador.
     *
     * @param id identificador del usuario
     * @return DTO de respuesta con el usuario encontrado
     * @throws IllegalArgumentException si el usuario no existe o está eliminado
     */
    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        User user = userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        return mapToResponse(user);
    }

    /**
     * Lista todos los usuarios activos con filtros opcionales por rol y estado.
     *
     * @param role     rol a filtrar (opcional)
     * @param status   estado a filtrar (opcional)
     * @param pageable información de paginación y ordenamiento
     * @return página de usuarios activos
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> findAll(Role role, Status status, Pageable pageable) {
        Page<User> pageResult;
        if (role != null && status != null) {
            pageResult = userRepository.findByDeletedAtIsNullAndRoleAndStatus(role, status, pageable);
        } else if (role != null) {
            pageResult = userRepository.findByDeletedAtIsNullAndRole(role, pageable);
        } else if (status != null) {
            pageResult = userRepository.findByDeletedAtIsNullAndStatus(status, pageable);
        } else {
            pageResult = userRepository.findByDeletedAtIsNull(pageable);
        }
        return pageResult.map(this::mapToResponse);
    }

    /**
     * Actualiza un usuario existente.
     * Valida que el email no esté duplicado.
     * No modifica la contraseña.
     *
     * @param id      identificador del usuario a actualizar
     * @param request nuevos datos del usuario
     * @return DTO de respuesta con el usuario actualizado
     * @throws IllegalArgumentException si el usuario no existe o el email ya está en uso
     */
    @Transactional
    public UserResponse update(Long id, UserUpdateRequest request) {
        User user = userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        if (!user.getEmail().equals(request.email())
                && userRepository.existsByEmailAndDeletedAtIsNullAndIdNot(request.email(), id)) {
            throw new IllegalArgumentException("Email already in use: " + request.email());
        }

        user.setName(request.name());
        user.setEmail(request.email());
        user.setRole(request.role());
        user.setStatus(request.status());

        User saved = userRepository.save(user);
        return mapToResponse(saved);
    }

    /**
     * Elimina un usuario de forma lógica (soft delete).
     *
     * @param id identificador del usuario a eliminar
     * @throws IllegalArgumentException si el usuario no existe o ya está eliminado
     */
    @Transactional
    public void delete(Long id) {
        User user = userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * Convierte una entidad User a su DTO de respuesta.
     * No incluye el hash de la contraseña.
     *
     * @param user entidad a convertir
     * @return DTO de respuesta con los datos del usuario
     */
    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getDeletedAt()
        );
    }
}
