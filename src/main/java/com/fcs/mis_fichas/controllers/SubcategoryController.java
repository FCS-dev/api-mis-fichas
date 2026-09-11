package com.fcs.mis_fichas.controllers;

import com.fcs.mis_fichas.config.Idempotent;
import com.fcs.mis_fichas.dtos.*;
import com.fcs.mis_fichas.services.SubcategoryService;
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
 * Controlador REST para la gestion de subcategorías.
 * Proporciona endpoints para crear, actualizar, eliminar y listar subcategorías.
 * Requiere autenticación JWT para todos los endpoints.
 */
@RestController
@RequestMapping("/subcategories")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER','ADMIN')")
@Tag(name = "Subcategorías", description = "Gestión de subcategorías (USER y ADMIN)")
public class SubcategoryController {

    private final SubcategoryService subcategoryService;

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;
    private static final String DEFAULT_SORT = "name,asc";

    /**
     * Crea una nueva subcategoría.
     * ADMIN: crea subcategorías del sistema.
     * USER: crea subcategorías personales.
     *
     * @param request     datos de la subcategoría a crear
     * @param httpRequest solicitud HTTP
     * @return ResponseEntity con ApiResponse de la subcategoría creada (HTTP 201)
     */
    @PostMapping
    @Idempotent
    @Operation(summary = "Crear subcategoría", description = "Crea una nueva subcategoría. ADMIN crea subcategorías del sistema; USER crea subcategorías personales.")
    public ResponseEntity<ApiResponse<SubcategoryResponse>> create(@Valid @RequestBody SubcategoryRequest request, HttpServletRequest httpRequest) {
        SubcategoryResponse response = subcategoryService.create(request);
        ApiResponse<SubcategoryResponse> apiResponse = new ApiResponse<>(
                true, HttpStatus.CREATED.value(), null, response,
                LocalDateTime.now(), httpRequest.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    /**
     * Actualiza una subcategoría existente.
     * ADMIN: puede modificar cualquier subcategoría.
     * USER: solo puede modificar sus propias subcategorías.
     *
     * @param id          identificador de la subcategoría a actualizar
     * @param request     nuevos datos de la subcategoría
     * @param httpRequest solicitud HTTP
     * @return ResponseEntity con ApiResponse de la subcategoría actualizada (HTTP 200)
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar subcategoría", description = "Actualiza una subcategoría existente. USER solo puede modificar las propias.")
    public ResponseEntity<ApiResponse<SubcategoryResponse>> update(
            @Parameter(description = "ID de la subcategoría", example = "1") @PathVariable Long id,
            @Valid @RequestBody SubcategoryRequest request,
            HttpServletRequest httpRequest) {
        SubcategoryResponse response = subcategoryService.update(id, request);
        ApiResponse<SubcategoryResponse> apiResponse = new ApiResponse<>(
                true, HttpStatus.OK.value(), null, response,
                LocalDateTime.now(), httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Elimina una subcategoría de forma lógica (soft delete).
     * ADMIN: puede eliminar cualquier subcategoría.
     * USER: solo puede eliminar sus propias subcategorías.
     *
     * @param id          identificador de la subcategoría a eliminar
     * @param httpRequest solicitud HTTP
     * @return ResponseEntity con ApiResponse de mensaje de éxito (HTTP 200)
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar subcategoría (soft delete)", description = "Elimina una subcategoría de forma lógica. USER solo puede eliminar las propias.")
    public ResponseEntity<ApiResponse<String>> delete(
            @Parameter(description = "ID de la subcategoría", example = "1") @PathVariable Long id,
            HttpServletRequest httpRequest) {
        subcategoryService.delete(id);
        ApiResponse<String> apiResponse = new ApiResponse<>(
                true, HttpStatus.OK.value(), "Subcategory deleted successfully", null,
                LocalDateTime.now(), httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Busca una subcategoría por su identificador.
     * ADMIN: puede ver cualquier subcategoría.
     * USER: puede ver las del sistema y las propias.
     *
     * @param id          identificador de la subcategoría
     * @param httpRequest solicitud HTTP
     * @return ResponseEntity con ApiResponse de la subcategoría encontrada (HTTP 200)
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener subcategoría por ID", description = "Devuelve los datos de una subcategoría específica.")
    public ResponseEntity<ApiResponse<SubcategoryResponse>> findById(
            @Parameter(description = "ID de la subcategoría", example = "1") @PathVariable Long id,
            HttpServletRequest httpRequest) {
        SubcategoryResponse response = subcategoryService.findById(id);
        ApiResponse<SubcategoryResponse> apiResponse = new ApiResponse<>(
                true, HttpStatus.OK.value(), null, response,
                LocalDateTime.now(), httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Lista todas las subcategorías activas de forma paginada.
     * ADMIN: ve todas las subcategorías.
     * USER: ve solo las del sistema y las propias.
     *
     * @param page        número de página (opcional, default 0)
     * @param size        cantidad de elementos por página (opcional, default 20, max 100)
     * @param sort        criterio de ordenamiento (opcional, default "name,asc")
     * @param httpRequest solicitud HTTP
     * @return ResponseEntity con ApiResponse de PagedResponse (HTTP 200)
     */
    @GetMapping
    @Operation(summary = "Listar subcategorías", description = "Lista todas las subcategorías activas paginadas. USER ve solo las del sistema y las propias.")
    public ResponseEntity<ApiResponse<PagedResponse<SubcategoryResponse>>> findAll(
            @Parameter(description = "Número de página (0-based)", example = "0") @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (max 100)", example = "20") @RequestParam(required = false, defaultValue = "20") int size,
            @Parameter(description = "Criterio de ordenamiento (propiedad,dirección)", example = "name,asc") @RequestParam(required = false, defaultValue = "name,asc") String sort,
            HttpServletRequest httpRequest) {
        Pageable pageable = buildPageable(page, size, sort);
        Page<SubcategoryResponse> pageResult = subcategoryService.findAll(pageable);
        PagedResponse<SubcategoryResponse> pagedResponse = mapToPagedResponse(pageResult);
        ApiResponse<PagedResponse<SubcategoryResponse>> apiResponse = new ApiResponse<>(
                true, HttpStatus.OK.value(), null, pagedResponse,
                LocalDateTime.now(), httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Lista subcategorías activas de una categoría específica de forma paginada.
     * ADMIN: ve todas las subcategorías de la categoría.
     * USER: ve solo las del sistema y las propias dentro de esa categoría.
     *
     * @param categoryId  identificador de la categoría
     * @param page        número de página (opcional, default 0)
     * @param size        cantidad de elementos por página (opcional, default 20, max 100)
     * @param sort        criterio de ordenamiento (opcional, default "name,asc")
     * @param httpRequest solicitud HTTP
     * @return ResponseEntity con ApiResponse de PagedResponse (HTTP 200)
     */
    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Listar subcategorías por categoría", description = "Lista las subcategorías activas de una categoría específica paginadas. ADMIN ve todas; USER ve las del sistema y las propias.")
    public ResponseEntity<ApiResponse<PagedResponse<SubcategoryResponse>>> findByCategoryId(
            @Parameter(description = "ID de la categoría", example = "1") @PathVariable Long categoryId,
            @Parameter(description = "Número de página (0-based)", example = "0") @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (max 100)", example = "20") @RequestParam(required = false, defaultValue = "20") int size,
            @Parameter(description = "Criterio de ordenamiento (propiedad,dirección)", example = "name,asc") @RequestParam(required = false, defaultValue = "name,asc") String sort,
            HttpServletRequest httpRequest) {
        Pageable pageable = buildPageable(page, size, sort);
        Page<SubcategoryResponse> pageResult = subcategoryService.findByCategoryId(categoryId, pageable);
        PagedResponse<SubcategoryResponse> pagedResponse = mapToPagedResponse(pageResult);
        ApiResponse<PagedResponse<SubcategoryResponse>> apiResponse = new ApiResponse<>(
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

    private PagedResponse<SubcategoryResponse> mapToPagedResponse(Page<SubcategoryResponse> page) {
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
