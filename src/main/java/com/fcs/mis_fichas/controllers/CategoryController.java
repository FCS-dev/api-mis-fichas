package com.fcs.mis_fichas.controllers;

import com.fcs.mis_fichas.dtos.CategoryRequest;
import com.fcs.mis_fichas.dtos.CategoryResponse;
import com.fcs.mis_fichas.services.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/**
 * Controlador REST para la gestion de categorias.
 * Todos los endpoints requieren rol ADMIN.
 * Proporciona operaciones CRUD para categorias con soporte de paginacion.
 */
@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Crea una nueva categoria.
     *
     * @param request datos de la categoria a crear
     * @return ResponseEntity con la categoria creada (HTTP 201)
     */
    @PostMapping
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.create(request);
        return ResponseEntity.created(URI.create("/admin/categories/" + response.id())).body(response);
    }

    /**
     * Actualiza una categoria existente.
     *
     * @param id      identificador de la categoria a actualizar
     * @param request nuevos datos de la categoria
     * @return ResponseEntity con la categoria actualizada (HTTP 200)
     */
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.update(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Elimina una categoria de forma logica (soft delete).
     *
     * @param id identificador de la categoria a eliminar
     * @return ResponseEntity vacio (HTTP 204)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Busca una categoria por su identificador.
     *
     * @param id identificador de la categoria
     * @return ResponseEntity con la categoria encontrada (HTTP 200)
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> findById(@PathVariable Long id) {
        CategoryResponse response = categoryService.findById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Lista todas las categorias activas de forma paginada.
     *
     * @param pageable informacion de paginacion (default: size=20, sort=name)
     * @return ResponseEntity con pagina de categorias (HTTP 200)
     */
    @GetMapping
    public ResponseEntity<Page<CategoryResponse>> findAll(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        Page<CategoryResponse> page = categoryService.findAll(pageable);
        return ResponseEntity.ok(page);
    }
}
