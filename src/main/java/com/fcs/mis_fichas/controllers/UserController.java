package com.fcs.mis_fichas.controllers;

import com.fcs.mis_fichas.dtos.*;
import com.fcs.mis_fichas.enums.Role;
import com.fcs.mis_fichas.enums.Status;
import com.fcs.mis_fichas.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Controlador REST para la gestión de usuarios.
 * Todos los endpoints requieren rol ADMIN.
 * Proporciona operaciones CRUD para usuarios con soporte de paginación
 * y filtros por rol y estado.
 */
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Usuarios", description = "Gestión de usuarios (solo ADMIN)")
public class UserController {

    private final UserService userService;

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;
    private static final String DEFAULT_SORT = "name,asc";

    /**
     * Busca un usuario por su identificador.
     *
     * @param id          identificador del usuario
     * @param httpRequest solicitud HTTP
     * @return ResponseEntity con ApiResponse del usuario encontrado (HTTP 200)
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID", description = "Devuelve los datos de un usuario específico.")
    public ResponseEntity<ApiResponse<UserResponse>> findById(
            @Parameter(description = "ID del usuario", example = "1") @PathVariable Long id,
            HttpServletRequest httpRequest) {
        UserResponse response = userService.findById(id);
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>(
                true, HttpStatus.OK.value(), null, response,
                LocalDateTime.now(), httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Lista todos los usuarios activos de forma paginada.
     * Soporta filtros opcionales por rol y estado.
     *
     * @param role        rol a filtrar (opcional)
     * @param status      estado a filtrar (opcional)
     * @param page        número de página (opcional, default 0)
     * @param size        cantidad de elementos por página (opcional, default 20, max 100)
     * @param sort        criterio de ordenamiento (opcional, default "name,asc")
     * @param httpRequest solicitud HTTP
     * @return ResponseEntity con ApiResponse de PagedResponse (HTTP 200)
     */
    @GetMapping
    @Operation(summary = "Listar usuarios", description = "Lista todos los usuarios activos paginados. Permite filtrar por rol y estado.")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> findAll(
            @Parameter(description = "Rol a filtrar", example = "USER") @RequestParam(required = false) Role role,
            @Parameter(description = "Estado a filtrar", example = "ACTIVE") @RequestParam(required = false) Status status,
            @Parameter(description = "Número de página (0-based)", example = "0") @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (max 100)", example = "20") @RequestParam(required = false, defaultValue = "20") int size,
            @Parameter(description = "Criterio de ordenamiento (propiedad,dirección)", example = "name,asc") @RequestParam(required = false, defaultValue = "name,asc") String sort,
            HttpServletRequest httpRequest) {
        Pageable pageable = buildPageable(page, size, sort);
        Page<UserResponse> pageResult = userService.findAll(role, status, pageable);
        PagedResponse<UserResponse> pagedResponse = mapToPagedResponse(pageResult);
        ApiResponse<PagedResponse<UserResponse>> apiResponse = new ApiResponse<>(
                true, HttpStatus.OK.value(), null, pagedResponse,
                LocalDateTime.now(), httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Actualiza un usuario existente.
     * Permite modificar nombre, email, rol y estado (incluyendo BLOCKED).
     *
     * @param id          identificador del usuario a actualizar
     * @param request     nuevos datos del usuario
     * @param httpRequest solicitud HTTP
     * @return ResponseEntity con ApiResponse del usuario actualizado (HTTP 200)
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario", description = "Permite modificar nombre, email, rol y estado (incluyendo BLOCKED).")
    public ResponseEntity<ApiResponse<UserResponse>> update(
            @Parameter(description = "ID del usuario a actualizar", example = "1") @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request,
            HttpServletRequest httpRequest) {
        UserResponse response = userService.update(id, request);
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>(
                true, HttpStatus.OK.value(), null, response,
                LocalDateTime.now(), httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Elimina un usuario de forma lógica (soft delete).
     *
     * @param id          identificador del usuario a eliminar
     * @param httpRequest solicitud HTTP
     * @return ResponseEntity con ApiResponse de mensaje de éxito (HTTP 200)
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario (soft delete)", description = "Elimina un usuario de forma lógica marcando deletedAt.")
    public ResponseEntity<ApiResponse<String>> delete(
            @Parameter(description = "ID del usuario a eliminar", example = "1") @PathVariable Long id,
            HttpServletRequest httpRequest) {
        userService.delete(id);
        ApiResponse<String> apiResponse = new ApiResponse<>(
                true, HttpStatus.OK.value(), "User deleted successfully", null,
                LocalDateTime.now(), httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(apiResponse);
    }

    private Pageable buildPageable(int page, int size, String sort) {
        int effectivePage = Math.max(page, DEFAULT_PAGE);
        int effectiveSize = Math.min(Math.max(size, 1), MAX_SIZE);
        Sort effectiveSort = parseSort(sort);
        return PageRequest.of(effectivePage, effectiveSize, effectiveSort);
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(DEFAULT_SORT.split(",")[0]).ascending();
        }
        String[] parts = sort.split(",");
        String property = parts[0].trim();
        if (parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim())) {
            return Sort.by(property).ascending();
        }
        return Sort.by(property).descending();
    }

    private PagedResponse<UserResponse> mapToPagedResponse(Page<UserResponse> page) {
        PaginationInfo pagination = new PaginationInfo(
                page.getNumber(),
                page.getSize(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.isFirst(),
                page.isLast(),
                page.getSort().toString()
        );
        return new PagedResponse<>(page.getContent(), pagination);
    }
}
