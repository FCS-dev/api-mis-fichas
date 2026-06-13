package com.fcs.mis_fichas.controllers;

import com.fcs.mis_fichas.dtos.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Manejador global de excepciones para la API.
 * Captura y formatea todas las excepciones en respuestas JSON estandarizadas
 * usando el wrapper {@link ApiResponse} con {@code data = null}.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Maneja excepciones de argumentos inválidos (lógica de negocio y validaciones manuales).
     * Devuelve HTTP 400 Bad Request.
     *
     * @param ex      excepción lanzada
     * @param request solicitud HTTP
     * @return ApiResponse con success=false, message=detalle del error, data=null
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("IllegalArgumentException en {}: {}", request.getRequestURI(), ex.getMessage());
        ApiResponse<Void> response = new ApiResponse<>(
                false,
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                null,
                LocalDateTime.now(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Maneja excepciones de credenciales inválidas (autenticación fallida).
     * Devuelve HTTP 401 Unauthorized.
     *
     * @param ex      excepción lanzada
     * @param request solicitud HTTP
     * @return ApiResponse con success=false, data=null
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        log.warn("BadCredentialsException en {}: {}", request.getRequestURI(), ex.getMessage());
        ApiResponse<Void> response = new ApiResponse<>(
                false,
                HttpStatus.UNAUTHORIZED.value(),
                "Invalid credentials",
                null,
                LocalDateTime.now(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Maneja excepciones de validación de bean (@Valid).
     * Devuelve HTTP 400 Bad Request con la lista de campos inválidos.
     *
     * @param ex      excepción de validación
     * @param request solicitud HTTP
     * @return ApiResponse con success=false, message=lista de errores, data=null
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("Validation failed en {}: {}", request.getRequestURI(), message);
        ApiResponse<Void> response = new ApiResponse<>(
                false,
                HttpStatus.BAD_REQUEST.value(),
                message,
                null,
                LocalDateTime.now(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Maneja excepciones de tiempo de ejecución no controladas.
     * Devuelve HTTP 500 Internal Server Error.
     *
     * @param ex      excepción lanzada
     * @param request solicitud HTTP
     * @return ApiResponse con success=false, data=null
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntime(RuntimeException ex, HttpServletRequest request) {
        log.error("RuntimeException en {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        ApiResponse<Void> response = new ApiResponse<>(
                false,
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getMessage(),
                null,
                LocalDateTime.now(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Manejador fallback para cualquier excepción no capturada específicamente.
     * Devuelve HTTP 500 Internal Server Error.
     *
     * @param ex      excepción lanzada
     * @param request solicitud HTTP
     * @return ApiResponse con success=false, data=null
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Unexpected exception en {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        ApiResponse<Void> response = new ApiResponse<>(
                false,
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred",
                null,
                LocalDateTime.now(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
