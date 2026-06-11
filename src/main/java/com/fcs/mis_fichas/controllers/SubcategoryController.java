package com.fcs.mis_fichas.controllers;

import com.fcs.mis_fichas.dtos.SubcategoryRequest;
import com.fcs.mis_fichas.dtos.SubcategoryResponse;
import com.fcs.mis_fichas.services.SubcategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/**
 * Controlador REST para la gestion de subcategorias.
 * Proporciona endpoints para crear, actualizar, eliminar y listar subcategorias.
 * Requiere autenticacion JWT para todos los endpoints.
 */
@RestController
@RequestMapping("/subcategories")
@RequiredArgsConstructor
public class SubcategoryController {

    private final SubcategoryService subcategoryService;

    /**
     * Crea una nueva subcategoria.
     * ADMIN: crea subcategorias del sistema.
     * USER: crea subcategorias personales.
     *
     * @param request datos de la subcategoria a crear
     * @return ResponseEntity con la subcategoria creada (HTTP 201)
     */
    @PostMapping
    public ResponseEntity<SubcategoryResponse> create(@Valid @RequestBody SubcategoryRequest request) {
        SubcategoryResponse response = subcategoryService.create(request);
        return ResponseEntity.created(URI.create("/subcategories/" + response.id())).body(response);
    }

    /**
     * Actualiza una subcategoria existente.
     * ADMIN: puede modificar cualquier subcategoria.
     * USER: solo puede modificar sus propias subcategorias.
     *
     * @param id      identificador de la subcategoria a actualizar
     * @param request nuevos datos de la subcategoria
     * @return ResponseEntity con la subcategoria actualizada (HTTP 200)
     */
    @PutMapping("/{id}")
    public ResponseEntity<SubcategoryResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody SubcategoryRequest request) {
        SubcategoryResponse response = subcategoryService.update(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Elimina una subcategoria de forma logica (soft delete).
     * ADMIN: puede eliminar cualquier subcategoria.
     * USER: solo puede eliminar sus propias subcategorias.
     *
     * @param id identificador de la subcategoria a eliminar
     * @return ResponseEntity vacio (HTTP 204)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        subcategoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Busca una subcategoria por su identificador.
     * ADMIN: puede ver cualquier subcategoria.
     * USER: puede ver las del sistema y las propias.
     *
     * @param id identificador de la subcategoria
     * @return ResponseEntity con la subcategoria encontrada (HTTP 200)
     */
    @GetMapping("/{id}")
    public ResponseEntity<SubcategoryResponse> findById(@PathVariable Long id) {
        SubcategoryResponse response = subcategoryService.findById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Lista todas las subcategorias activas de forma paginada.
     * ADMIN: ve todas las subcategorias.
     * USER: ve solo las del sistema y las propias.
     *
     * @param pageable informacion de paginacion (default: size=20, sort=name)
     * @return ResponseEntity con pagina de subcategorias (HTTP 200)
     */
    @GetMapping
    public ResponseEntity<Page<SubcategoryResponse>> findAll(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        Page<SubcategoryResponse> page = subcategoryService.findAll(pageable);
        return ResponseEntity.ok(page);
    }
}
