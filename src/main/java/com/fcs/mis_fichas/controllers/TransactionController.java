package com.fcs.mis_fichas.controllers;

import com.fcs.mis_fichas.dtos.TransactionRequest;
import com.fcs.mis_fichas.dtos.TransactionResponse;
import com.fcs.mis_fichas.services.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;

/**
 * Controlador REST para la gestion de transacciones.
 * Proporciona endpoints para crear, actualizar, eliminar y consultar transacciones
 * con soporte de filtros por categoria, subcategoria, fecha y rango de fechas.
 * Requiere autenticacion JWT para todos los endpoints.
 */
@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * Crea una nueva transaccion.
     * USER: solo puede crear transacciones para si mismo.
     * ADMIN: no puede crear transacciones.
     *
     * @param request datos de la transaccion a crear
     * @return ResponseEntity con la transaccion creada (HTTP 201)
     */
    @PostMapping
    public ResponseEntity<TransactionResponse> create(@Valid @RequestBody TransactionRequest request) {
        TransactionResponse response = transactionService.create(request);
        return ResponseEntity.created(URI.create("/transactions/" + response.id())).body(response);
    }

    /**
     * Actualiza una transaccion existente.
     * USER: solo puede modificar sus propias transacciones.
     * ADMIN: puede modificar transacciones de cualquier USER.
     *
     * @param id      identificador de la transaccion a actualizar
     * @param request nuevos datos de la transaccion
     * @return ResponseEntity con la transaccion actualizada (HTTP 200)
     */
    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequest request) {
        TransactionResponse response = transactionService.update(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Elimina una transaccion de forma logica (soft delete).
     * USER: solo puede eliminar sus propias transacciones.
     * ADMIN: puede eliminar transacciones de cualquier USER.
     *
     * @param id identificador de la transaccion a eliminar
     * @return ResponseEntity vacio (HTTP 204)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        transactionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Busca una transaccion por su identificador.
     * USER: solo puede ver sus propias transacciones.
     * ADMIN: puede ver cualquier transaccion.
     *
     * @param id identificador de la transaccion
     * @return ResponseEntity con la transaccion encontrada (HTTP 200)
     */
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> findById(@PathVariable Long id) {
        TransactionResponse response = transactionService.findById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Lista transacciones con filtros opcionales y paginacion.
     * USER: siempre filtra por su propio ID.
     * ADMIN: puede filtrar por cualquier usuario o ver todos.
     *
     * @param userId        identificador de usuario a filtrar (opcional, solo ADMIN)
     * @param categoryId    identificador de categoria a filtrar (opcional)
     * @param subcategoryId identificador de subcategoria a filtrar (opcional)
     * @param date          fecha exacta a filtrar (opcional)
     * @param dateFrom      fecha inicio de rango a filtrar (opcional)
     * @param dateTo        fecha fin de rango a filtrar (opcional)
     * @param pageable      informacion de paginacion (default: size=20, sort=transactionDate,desc)
     * @return ResponseEntity con pagina de transacciones (HTTP 200)
     */
    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> findAll(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long subcategoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @PageableDefault(size = 20, sort = "transactionDate,desc") Pageable pageable) {
        Page<TransactionResponse> page = transactionService.findAll(userId, categoryId, subcategoryId, date, dateFrom, dateTo, pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * Lista transacciones filtradas por categoria.
     *
     * @param categoryId identificador de la categoria
     * @param userId     identificador de usuario a filtrar (opcional, solo ADMIN)
     * @param pageable   informacion de paginacion
     * @return ResponseEntity con pagina de transacciones (HTTP 200)
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<TransactionResponse>> findByCategory(
            @PathVariable Long categoryId,
            @RequestParam(required = false) Long userId,
            @PageableDefault(size = 20, sort = "transactionDate,desc") Pageable pageable) {
        Page<TransactionResponse> page = transactionService.findAll(userId, categoryId, null, null, null, null, pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * Lista transacciones filtradas por subcategoria.
     *
     * @param subcategoryId identificador de la subcategoria
     * @param userId        identificador de usuario a filtrar (opcional, solo ADMIN)
     * @param pageable      informacion de paginacion
     * @return ResponseEntity con pagina de transacciones (HTTP 200)
     */
    @GetMapping("/subcategory/{subcategoryId}")
    public ResponseEntity<Page<TransactionResponse>> findBySubcategory(
            @PathVariable Long subcategoryId,
            @RequestParam(required = false) Long userId,
            @PageableDefault(size = 20, sort = "transactionDate,desc") Pageable pageable) {
        Page<TransactionResponse> page = transactionService.findAll(userId, null, subcategoryId, null, null, null, pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * Lista transacciones filtradas por fecha exacta.
     *
     * @param date     fecha exacta de las transacciones
     * @param userId   identificador de usuario a filtrar (opcional, solo ADMIN)
     * @param pageable informacion de paginacion
     * @return ResponseEntity con pagina de transacciones (HTTP 200)
     */
    @GetMapping("/date/{date}")
    public ResponseEntity<Page<TransactionResponse>> findByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long userId,
            @PageableDefault(size = 20, sort = "transactionDate,desc") Pageable pageable) {
        Page<TransactionResponse> page = transactionService.findAll(userId, null, null, date, null, null, pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * Lista transacciones filtradas por rango de fechas.
     *
     * @param from     fecha de inicio del rango
     * @param to       fecha de fin del rango
     * @param userId   identificador de usuario a filtrar (opcional, solo ADMIN)
     * @param pageable informacion de paginacion
     * @return ResponseEntity con pagina de transacciones (HTTP 200)
     */
    @GetMapping("/date-range")
    public ResponseEntity<Page<TransactionResponse>> findByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long userId,
            @PageableDefault(size = 20, sort = "transactionDate,desc") Pageable pageable) {
        Page<TransactionResponse> page = transactionService.findAll(userId, null, null, null, from, to, pageable);
        return ResponseEntity.ok(page);
    }
}
