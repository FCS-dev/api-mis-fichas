package com.fcs.mis_fichas.controllers;

import com.fcs.mis_fichas.dtos.*;
import com.fcs.mis_fichas.services.CategoryService;
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
 * Controlador REST para la gestion de categorías.
 * Todos los endpoints requieren rol ADMIN.
 * Proporciona operaciones CRUD para categorías con soporte de paginación.
 */
@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Categorías", description = "Gestión de categorías (solo ADMIN)")
public class CategoryController {

    private final CategoryService categoryService;

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;
    private static final String DEFAULT_SORT = "name,asc";

    /**
     * Crea una nueva categoria.
     *
     * @param request     datos de la categoria a crear
     * @param httpRequest solicitud HTTP
     * @return ResponseEntity con ApiResponse de la categoria creada (HTTP 201)
     */
    @PostMapping
    @Operation(summary = "Crear categoría", description = "Crea una nueva categoría de ingreso o gasto.")
    public ResponseEntity<ApiResponse<CategoryResponse>> create(@Valid @RequestBody CategoryRequest request, HttpServletRequest httpRequest) {
        CategoryResponse response = categoryService.create(request);
        ApiResponse<CategoryResponse> apiResponse = new ApiResponse<>(
                true, HttpStatus.CREATED.value(), null, response,
                LocalDateTime.now(), httpRequest.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    /**
     * Actualiza una categoria existente.
     *
     * @param id          identificador de la categoria a actualizar
     * @param request     nuevos datos de la categoria
     * @param httpRequest solicitud HTTP
     * @return ResponseEntity con ApiResponse de la categoria actualizada (HTTP 200)
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar categoría", description = "Actualiza los datos de una categoría existente.")
    public ResponseEntity<ApiResponse<CategoryResponse>> update(
            @Parameter(description = "ID de la categoría", example = "1") @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request,
            HttpServletRequest httpRequest) {
        CategoryResponse response = categoryService.update(id, request);
        ApiResponse<CategoryResponse> apiResponse = new ApiResponse<>(
                true, HttpStatus.OK.value(), null, response,
                LocalDateTime.now(), httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Elimina una categoria de forma lógica (soft delete).
     *
     * @param id          identificador de la categoria a eliminar
     * @param httpRequest solicitud HTTP
     * @return ResponseEntity con ApiResponse de mensaje de éxito (HTTP 200)
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar categoría (soft delete)", description = "Elimina una categoría de forma lógica.")
    public ResponseEntity<ApiResponse<String>> delete(
            @Parameter(description = "ID de la categoría", example = "1") @PathVariable Long id,
            HttpServletRequest httpRequest) {
        categoryService.delete(id);
        ApiResponse<String> apiResponse = new ApiResponse<>(
                true, HttpStatus.OK.value(), "Category deleted successfully", null,
                LocalDateTime.now(), httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Busca una categoria por su identificador.
     *
     * @param id          identificador de la categoria
     * @param httpRequest solicitud HTTP
     * @return ResponseEntity con ApiResponse de la categoria encontrada (HTTP 200)
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener categoría por ID", description = "Devuelve los datos de una categoría específica.")
    public ResponseEntity<ApiResponse<CategoryResponse>> findById(
            @Parameter(description = "ID de la categoría", example = "1") @PathVariable Long id,
            HttpServletRequest httpRequest) {
        CategoryResponse response = categoryService.findById(id);
        ApiResponse<CategoryResponse> apiResponse = new ApiResponse<>(
                true, HttpStatus.OK.value(), null, response,
                LocalDateTime.now(), httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Lista todas las categorías activas de forma paginada.
     *
     * @param page        número de página (opcional, default 0)
     * @param size        cantidad de elementos por página (opcional, default 20, max 100)
     * @param sort        criterio de ordenamiento (opcional, default "name,asc")
     * @param httpRequest solicitud HTTP
     * @return ResponseEntity con ApiResponse de PagedResponse (HTTP 200)
     */
    @GetMapping
    @Operation(summary = "Listar categorías", description = "Lista todas las categorías activas paginadas.")
    public ResponseEntity<ApiResponse<PagedResponse<CategoryResponse>>> findAll(
            @Parameter(description = "Número de página (0-based)", example = "0") @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (max 100)", example = "20") @RequestParam(required = false, defaultValue = "20") int size,
            @Parameter(description = "Criterio de ordenamiento (propiedad,dirección)", example = "name,asc") @RequestParam(required = false, defaultValue = "name,asc") String sort,
            HttpServletRequest httpRequest) {
        Pageable pageable = buildPageable(page, size, sort);
        Page<CategoryResponse> pageResult = categoryService.findAll(pageable);
        PagedResponse<CategoryResponse> pagedResponse = mapToPagedResponse(pageResult);
        ApiResponse<PagedResponse<CategoryResponse>> apiResponse = new ApiResponse<>(
                true, HttpStatus.OK.value(), null, pagedResponse,
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

    private PagedResponse<CategoryResponse> mapToPagedResponse(Page<CategoryResponse> page) {
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
