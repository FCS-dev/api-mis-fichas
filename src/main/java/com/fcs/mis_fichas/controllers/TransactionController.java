package com.fcs.mis_fichas.controllers;

import com.fcs.mis_fichas.dtos.*;
import com.fcs.mis_fichas.services.TransactionService;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Controlador REST para la gestion de transacciones.
 * Proporciona endpoints para crear, actualizar, eliminar y consultar transacciones
 * con soporte de filtros por categoria, subcategoría, fecha y rango de fechas.
 * Requiere autenticación JWT para todos los endpoints.
 */
@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER','ADMIN')")
@Tag(name = "Transacciones", description = "Gestión de transacciones de ingresos y gastos (USER y ADMIN)")
public class TransactionController {

    private final TransactionService transactionService;

    private static final int DEFAULT_PAGE = 0;
    private static final int MAX_SIZE = 100;
    private static final String DEFAULT_SORT = "transactionDate,desc";

    /**
     * Crea una nueva transacción.
     * USER: solo puede crear transacciones para si mismo.
     * ADMIN: no puede crear transacciones.
     *
     * @param request     datos de la transacción a crear
     * @param httpRequest solicitud HTTP
     * @return ResponseEntity con ApiResponse de la transacción creada (HTTP 201)
     */
    @PostMapping
    @Operation(summary = "Crear transacción", description = "Crea una nueva transacción de ingreso o gasto.")
    public ResponseEntity<ApiResponse<TransactionResponse>> create(@Valid @RequestBody TransactionRequest request, HttpServletRequest httpRequest) {
        TransactionResponse response = transactionService.create(request);
        ApiResponse<TransactionResponse> apiResponse = new ApiResponse<>(
                true, HttpStatus.CREATED.value(), null, response,
                LocalDateTime.now(), httpRequest.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    /**
     * Actualiza una transacción existente.
     * USER: solo puede modificar sus propias transacciones.
     * ADMIN: puede modificar transacciones de cualquier USER.
     *
     * @param id          identificador de la transacción a actualizar
     * @param request     nuevos datos de la transacción
     * @param httpRequest solicitud HTTP
     * @return ResponseEntity con ApiResponse de la transacción actualizada (HTTP 200)
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar transacción", description = "Actualiza una transacción existente. USER solo puede modificar las propias.")
    public ResponseEntity<ApiResponse<TransactionResponse>> update(
            @Parameter(description = "ID de la transacción", example = "1") @PathVariable Long id,
            @Valid @RequestBody TransactionRequest request,
            HttpServletRequest httpRequest) {
        TransactionResponse response = transactionService.update(id, request);
        ApiResponse<TransactionResponse> apiResponse = new ApiResponse<>(
                true, HttpStatus.OK.value(), null, response,
                LocalDateTime.now(), httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Elimina una transacción de forma lógica (soft delete).
     * USER: solo puede eliminar sus propias transacciones.
     * ADMIN: puede eliminar transacciones de cualquier USER.
     *
     * @param id          identificador de la transacción a eliminar
     * @param httpRequest solicitud HTTP
     * @return ResponseEntity con ApiResponse de mensaje de éxito (HTTP 200)
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar transacción (soft delete)", description = "Elimina una transacción de forma lógica.")
    public ResponseEntity<ApiResponse<String>> delete(
            @Parameter(description = "ID de la transacción", example = "1") @PathVariable Long id,
            HttpServletRequest httpRequest) {
        transactionService.delete(id);
        ApiResponse<String> apiResponse = new ApiResponse<>(
                true, HttpStatus.OK.value(), "Transaction deleted successfully", null,
                LocalDateTime.now(), httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Busca una transacción por su identificador.
     * USER: solo puede ver sus propias transacciones.
     * ADMIN: puede ver cualquier transacción.
     *
     * @param id          identificador de la transacción
     * @param httpRequest solicitud HTTP
     * @return ResponseEntity con ApiResponse de la transacción encontrada (HTTP 200)
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener transacción por ID", description = "Devuelve los datos de una transacción específica.")
    public ResponseEntity<ApiResponse<TransactionResponse>> findById(
            @Parameter(description = "ID de la transacción", example = "1") @PathVariable Long id,
            HttpServletRequest httpRequest) {
        TransactionResponse response = transactionService.findById(id);
        ApiResponse<TransactionResponse> apiResponse = new ApiResponse<>(
                true, HttpStatus.OK.value(), null, response,
                LocalDateTime.now(), httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Lista transacciones con filtros opcionales y paginación.
     * USER: siempre filtra por su propio ID.
     * ADMIN: puede filtrar por cualquier usuario o ver todos.
     *
     * @param userId        identificador de usuario a filtrar (opcional, solo ADMIN)
     * @param categoryId    identificador de categoria a filtrar (opcional)
     * @param subcategoryId identificador de subcategoría a filtrar (opcional)
     * @param date          fecha exacta a filtrar (opcional)
     * @param dateFrom      fecha inicio de rango a filtrar (opcional)
     * @param dateTo        fecha fin de rango a filtrar (opcional)
     * @param page          numero de pagina (opcional, default 0)
     * @param size          cantidad de elementos por página (opcional, default 20, max 100)
     * @param sort          criterio de ordenamiento (opcional, default "transactionDate,desc")
     * @param httpRequest   solicitud HTTP
     * @return ResponseEntity con ApiResponse de PagedResponse (HTTP 200)
     */
    @GetMapping
    @Operation(summary = "Listar transacciones", description = "Lista transacciones con filtros opcionales y paginación.")
    public ResponseEntity<ApiResponse<PagedResponse<TransactionResponse>>> findAll(
            @Parameter(description = "ID de usuario a filtrar (solo ADMIN)", example = "1") @RequestParam(required = false) Long userId,
            @Parameter(description = "ID de categoría a filtrar", example = "1") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "ID de subcategoría a filtrar", example = "1") @RequestParam(required = false) Long subcategoryId,
            @Parameter(description = "Fecha exacta (ISO)", example = "2024-01-15") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "Fecha inicio del rango (ISO)", example = "2024-01-01") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @Parameter(description = "Fecha fin del rango (ISO)", example = "2024-01-31") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @Parameter(description = "Número de página (0-based)", example = "0") @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (max 100)", example = "20") @RequestParam(required = false, defaultValue = "20") int size,
            @Parameter(description = "Criterio de ordenamiento (propiedad,dirección)", example = "transactionDate,desc") @RequestParam(required = false, defaultValue = "transactionDate,desc") String sort,
            HttpServletRequest httpRequest) {
        Pageable pageable = buildPageable(page, size, sort);
        Page<TransactionResponse> pageResult = transactionService.findAll(userId, categoryId, subcategoryId, date, dateFrom, dateTo, pageable);
        PagedResponse<TransactionResponse> pagedResponse = mapToPagedResponse(pageResult);
        ApiResponse<PagedResponse<TransactionResponse>> apiResponse = new ApiResponse<>(
                true, HttpStatus.OK.value(), null, pagedResponse,
                LocalDateTime.now(), httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Lista transacciones filtradas por categoria.
     *
     * @param categoryId  identificador de la categoria
     * @param userId      identificador de usuario a filtrar (opcional, solo ADMIN)
     * @param page        numero de pagina (opcional, default 0)
     * @param size        cantidad de elementos por página (opcional, default 20, max 100)
     * @param sort        criterio de ordenamiento (opcional, default "transactionDate,desc")
     * @param httpRequest solicitud HTTP
     * @return ResponseEntity con ApiResponse de PagedResponse (HTTP 200)
     */
    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Listar transacciones por categoría", description = "Lista transacciones filtradas por categoría.")
    public ResponseEntity<ApiResponse<PagedResponse<TransactionResponse>>> findByCategory(
            @Parameter(description = "ID de la categoría", example = "1") @PathVariable Long categoryId,
            @Parameter(description = "ID de usuario a filtrar (solo ADMIN)", example = "1") @RequestParam(required = false) Long userId,
            @Parameter(description = "Número de página (0-based)", example = "0") @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (max 100)", example = "20") @RequestParam(required = false, defaultValue = "20") int size,
            @Parameter(description = "Criterio de ordenamiento (propiedad,dirección)", example = "transactionDate,desc") @RequestParam(required = false, defaultValue = "transactionDate,desc") String sort,
            HttpServletRequest httpRequest) {
        Pageable pageable = buildPageable(page, size, sort);
        Page<TransactionResponse> pageResult = transactionService.findAll(userId, categoryId, null, null, null, null, pageable);
        PagedResponse<TransactionResponse> pagedResponse = mapToPagedResponse(pageResult);
        ApiResponse<PagedResponse<TransactionResponse>> apiResponse = new ApiResponse<>(
                true, HttpStatus.OK.value(), null, pagedResponse,
                LocalDateTime.now(), httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Lista transacciones filtradas por subcategoría.
     *
     * @param subcategoryId identificador de la subcategoría
     * @param userId        identificador de usuario a filtrar (opcional, solo ADMIN)
     * @param page          numero de pagina (opcional, default 0)
     * @param size          cantidad de elementos por página (opcional, default 20, max 100)
     * @param sort          criterio de ordenamiento (opcional, default "transactionDate,desc")
     * @param httpRequest   solicitud HTTP
     * @return ResponseEntity con ApiResponse de PagedResponse (HTTP 200)
     */
    @GetMapping("/subcategory/{subcategoryId}")
    @Operation(summary = "Listar transacciones por subcategoría", description = "Lista transacciones filtradas por subcategoría.")
    public ResponseEntity<ApiResponse<PagedResponse<TransactionResponse>>> findBySubcategory(
            @Parameter(description = "ID de la subcategoría", example = "1") @PathVariable Long subcategoryId,
            @Parameter(description = "ID de usuario a filtrar (solo ADMIN)", example = "1") @RequestParam(required = false) Long userId,
            @Parameter(description = "Número de página (0-based)", example = "0") @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (max 100)", example = "20") @RequestParam(required = false, defaultValue = "20") int size,
            @Parameter(description = "Criterio de ordenamiento (propiedad,dirección)", example = "transactionDate,desc") @RequestParam(required = false, defaultValue = "transactionDate,desc") String sort,
            HttpServletRequest httpRequest) {
        Pageable pageable = buildPageable(page, size, sort);
        Page<TransactionResponse> pageResult = transactionService.findAll(userId, null, subcategoryId, null, null, null, pageable);
        PagedResponse<TransactionResponse> pagedResponse = mapToPagedResponse(pageResult);
        ApiResponse<PagedResponse<TransactionResponse>> apiResponse = new ApiResponse<>(
                true, HttpStatus.OK.value(), null, pagedResponse,
                LocalDateTime.now(), httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Lista transacciones filtradas por fecha exacta.
     *
     * @param date        fecha exacta de las transacciones
     * @param userId      identificador de usuario a filtrar (opcional, solo ADMIN)
     * @param page        numero de pagina (opcional, default 0)
     * @param size        cantidad de elementos por página (opcional, default 20, max 100)
     * @param sort        criterio de ordenamiento (opcional, default "transactionDate,desc")
     * @param httpRequest solicitud HTTP
     * @return ResponseEntity con ApiResponse de PagedResponse (HTTP 200)
     */
    @GetMapping("/date/{date}")
    @Operation(summary = "Listar transacciones por fecha exacta", description = "Lista transacciones filtradas por una fecha exacta.")
    public ResponseEntity<ApiResponse<PagedResponse<TransactionResponse>>> findByDate(
            @Parameter(description = "Fecha exacta (ISO)", example = "2024-01-15") @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "ID de usuario a filtrar (solo ADMIN)", example = "1") @RequestParam(required = false) Long userId,
            @Parameter(description = "Número de página (0-based)", example = "0") @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (max 100)", example = "20") @RequestParam(required = false, defaultValue = "20") int size,
            @Parameter(description = "Criterio de ordenamiento (propiedad,dirección)", example = "transactionDate,desc") @RequestParam(required = false, defaultValue = "transactionDate,desc") String sort,
            HttpServletRequest httpRequest) {
        Pageable pageable = buildPageable(page, size, sort);
        Page<TransactionResponse> pageResult = transactionService.findAll(userId, null, null, date, null, null, pageable);
        PagedResponse<TransactionResponse> pagedResponse = mapToPagedResponse(pageResult);
        ApiResponse<PagedResponse<TransactionResponse>> apiResponse = new ApiResponse<>(
                true, HttpStatus.OK.value(), null, pagedResponse,
                LocalDateTime.now(), httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Lista transacciones filtradas por rango de fechas.
     *
     * @param from        fecha de inicio del rango
     * @param to          fecha de fin del rango
     * @param userId      identificador de usuario a filtrar (opcional, solo ADMIN)
     * @param page        numero de pagina (opcional, default 0)
     * @param size        cantidad de elementos por página (opcional, default 20, max 100)
     * @param sort        criterio de ordenamiento (opcional, default "transactionDate,desc")
     * @param httpRequest solicitud HTTP
     * @return ResponseEntity con ApiResponse de PagedResponse (HTTP 200)
     */
    @GetMapping("/date-range")
    @Operation(summary = "Listar transacciones por rango de fechas", description = "Lista transacciones filtradas por un rango de fechas. Si no se envían from/to, retorna todas las transacciones.")
    public ResponseEntity<ApiResponse<PagedResponse<TransactionResponse>>> findByDateRange(
            @Parameter(description = "Fecha de inicio (ISO)", example = "2024-01-01") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Fecha de fin (ISO)", example = "2024-01-31") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @Parameter(description = "ID de usuario a filtrar (solo ADMIN)", example = "1") @RequestParam(required = false) Long userId,
            @Parameter(description = "Número de página (0-based)", example = "0") @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (max 100)", example = "20") @RequestParam(required = false, defaultValue = "20") int size,
            @Parameter(description = "Criterio de ordenamiento (propiedad,dirección)", example = "transactionDate,desc") @RequestParam(required = false, defaultValue = "transactionDate,desc") String sort,
            HttpServletRequest httpRequest) {
        Pageable pageable = buildPageable(page, size, sort);
        Page<TransactionResponse> pageResult = transactionService.findAll(userId, null, null, null, from, to, pageable);
        PagedResponse<TransactionResponse> pagedResponse = mapToPagedResponse(pageResult);
        ApiResponse<PagedResponse<TransactionResponse>> apiResponse = new ApiResponse<>(
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
            return Sort.by(DEFAULT_SORT.split(",")[0]).descending();
        }
        String[] parts = sort.split(",");
        String property = parts[0].trim();
        if (parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim())) {
            return Sort.by(property).ascending();
        }
        return Sort.by(property).descending();
    }

    private PagedResponse<TransactionResponse> mapToPagedResponse(Page<TransactionResponse> page) {
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
