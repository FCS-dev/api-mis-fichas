package com.fcs.mis_fichas.controllers;

import com.fcs.mis_fichas.dtos.*;
import com.fcs.mis_fichas.repositories.RefreshTokenRepository;
import com.fcs.mis_fichas.services.BruteForceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador REST para operaciones de seguridad administrativas.
 * Todos los endpoints requieren rol ADMIN.
 * Permite gestionar bloqueos por fuerza bruta y limpiar tokens vencidos.
 */
@RestController
@RequestMapping("/admin/security")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Seguridad", description = "Gestión de seguridad (solo ADMIN)")
public class AdminSecurityController {

    private final BruteForceService bruteForceService;
    private final RefreshTokenRepository refreshTokenRepository;

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;
    private static final String DEFAULT_SORT = "email,asc";

    /**
     * Lista las cuentas bloqueadas por protección contra fuerza bruta.
     * Soporta paginación y ordenamiento por email o lockedUntil.
     *
     * @param page        número de página (0-based)
     * @param size        cantidad de elementos por página
     * @param sort        criterio de ordenamiento (propiedad,dirección)
     * @param httpRequest solicitud HTTP
     * @return ResponseEntity con ApiResponse de PagedResponse de cuentas bloqueadas
     */
    @GetMapping("/brute-force/blocked")
    @Operation(summary = "Listar cuentas bloqueadas",
            description = "Devuelve las cuentas bloqueadas por intentos fallidos de login, con paginación y ordenamiento.")
    public ResponseEntity<ApiResponse<PagedResponse<BlockedAccountResponse>>> getBlockedAccounts(
            @Parameter(description = "Número de página (0-based)", example = "0") @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (max 100)", example = "20") @RequestParam(required = false, defaultValue = "20") int size,
            @Parameter(description = "Criterio de ordenamiento (email,asc | email,desc | lockedUntil,asc | lockedUntil,desc)", example = "email,asc") @RequestParam(required = false, defaultValue = "email,asc") String sort,
            HttpServletRequest httpRequest) {

        List<BruteForceService.BlockedAccount> blocked = bruteForceService.getBlockedAccounts();

        String[] sortParts = sort.split(",");
        String property = sortParts[0].trim();
        boolean ascending = sortParts.length <= 1 || "asc".equalsIgnoreCase(sortParts[1].trim());

        List<BruteForceService.BlockedAccount> sorted = bruteForceService.sortBlockedAccounts(blocked, property, ascending);

        int effectivePage = Math.max(page, DEFAULT_PAGE);
        int effectiveSize = Math.min(Math.max(size, 1), MAX_SIZE);
        int totalElements = sorted.size();
        int totalPages = (int) Math.ceil((double) totalElements / effectiveSize);
        int start = effectivePage * effectiveSize;

        List<BlockedAccountResponse> content;
        if (start >= totalElements) {
            content = List.of();
        } else {
            int end = Math.min(start + effectiveSize, totalElements);
            content = sorted.subList(start, end).stream()
                    .map(ba -> new BlockedAccountResponse(ba.email(), ba.lockedUntil()))
                    .toList();
        }

        PaginationInfo pagination = new PaginationInfo(
                effectivePage,
                effectiveSize,
                totalPages,
                totalElements,
                effectivePage == 0,
                effectivePage >= totalPages - 1 || totalPages == 0,
                sort
        );

        PagedResponse<BlockedAccountResponse> pagedResponse = new PagedResponse<>(content, pagination);
        ApiResponse<PagedResponse<BlockedAccountResponse>> apiResponse = new ApiResponse<>(
                true, HttpStatus.OK.value(), null, pagedResponse,
                LocalDateTime.now(), httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Revoca manualmente el bloqueo de una cuenta para permitir el login inmediato.
     *
     * @param request     datos de la cuenta a desbloquear
     * @param httpRequest solicitud HTTP
     * @return ResponseEntity con ApiResponse de mensaje de éxito
     */
    @PostMapping("/brute-force/unblock")
    @Operation(summary = "Desbloquear cuenta", description = "Permite a un ADMIN revocar el bloqueo por fuerza bruta de una cuenta.")
    public ResponseEntity<ApiResponse<String>> unblock(
            @Valid @RequestBody UnblockRequest request,
            HttpServletRequest httpRequest) {

        boolean unblocked = bruteForceService.unblock(request.email());
        String message = unblocked
                ? "Cuenta desbloqueada correctamente"
                : "La cuenta no estaba bloqueada";

        ApiResponse<String> apiResponse = new ApiResponse<>(
                true, HttpStatus.OK.value(), message, null,
                LocalDateTime.now(), httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Elimina los refresh tokens revocados cuya fecha de expiración ya ha pasado.
     *
     * @param httpRequest solicitud HTTP
     * @return ResponseEntity con ApiResponse de cantidad de tokens eliminados
     */
    @PostMapping("/refresh-tokens/cleanup")
    @Operation(summary = "Limpiar refresh tokens vencidos",
            description = "Elimina físicamente los refresh tokens revocados cuya fecha de expiración ya pasó.")
    public ResponseEntity<ApiResponse<CleanupResponse>> cleanupRefreshTokens(HttpServletRequest httpRequest) {
        long deleted = refreshTokenRepository.deleteByRevokedAtIsNotNullAndExpiresAtBefore(LocalDateTime.now());

        ApiResponse<CleanupResponse> apiResponse = new ApiResponse<>(
                true, HttpStatus.OK.value(), null, new CleanupResponse(deleted),
                LocalDateTime.now(), httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(apiResponse);
    }
}
