package com.fcs.mis_fichas.controllers;

import com.fcs.mis_fichas.dtos.*;
import com.fcs.mis_fichas.services.TransactionService;
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
    public ResponseEntity<ApiResponse<TransactionResponse>> update(
            @PathVariable Long id,
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
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable Long id, HttpServletRequest httpRequest) {
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
    public ResponseEntity<ApiResponse<TransactionResponse>> findById(@PathVariable Long id, HttpServletRequest httpRequest) {
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
    public ResponseEntity<ApiResponse<PagedResponse<TransactionResponse>>> findAll(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long subcategoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "transactionDate,desc") String sort,
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
    public ResponseEntity<ApiResponse<PagedResponse<TransactionResponse>>> findByCategory(
            @PathVariable Long categoryId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "transactionDate,desc") String sort,
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
    public ResponseEntity<ApiResponse<PagedResponse<TransactionResponse>>> findBySubcategory(
            @PathVariable Long subcategoryId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "transactionDate,desc") String sort,
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
    public ResponseEntity<ApiResponse<PagedResponse<TransactionResponse>>> findByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "transactionDate,desc") String sort,
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
    public ResponseEntity<ApiResponse<PagedResponse<TransactionResponse>>> findByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "transactionDate,desc") String sort,
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
