package com.fcs.mis_fichas.controllers;

import com.fcs.mis_fichas.dtos.ApiResponse;
import com.fcs.mis_fichas.dtos.CategoryResponse;
import com.fcs.mis_fichas.dtos.PagedResponse;
import com.fcs.mis_fichas.dtos.PaginationInfo;
import com.fcs.mis_fichas.services.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * Controlador REST público para la consulta de categorías.
 * Permite a cualquier usuario autenticado listar las categorías activas.
 * Las operaciones de creación, actualización y eliminación siguen en {@link CategoryController}
 * bajo /admin/categories y requieren rol ADMIN.
 */
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Tag(name = "Categorías", description = "Consulta pública de categorías activas (requiere autenticación)")
public class CategoryPublicController {

    private final CategoryService categoryService;

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;
    private static final String DEFAULT_SORT = "name,asc";

    /**
     * Lista todas las categorías activas de forma paginada.
     * Requiere autenticación JWT.
     *
     * @param page        número de página (opcional, default 0)
     * @param size        cantidad de elementos por página (opcional, default 20, max 100)
     * @param sort        criterio de ordenamiento (opcional, default "name,asc")
     * @param httpRequest solicitud HTTP
     * @return ResponseEntity con ApiResponse de PagedResponse (HTTP 200)
     */
    @GetMapping
    @Operation(summary = "Listar categorías activas", description = "Lista todas las categorías activas paginadas. Requiere autenticación JWT.")
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
