package com.fcs.mis_fichas.controllers;

import com.fcs.mis_fichas.dtos.AuthResponse;
import com.fcs.mis_fichas.dtos.LoginRequest;
import com.fcs.mis_fichas.dtos.RegisterRequest;
import com.fcs.mis_fichas.services.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la autenticacion.
 * Proporciona endpoints publicos para registro, inicio de sesion,
 * refresco de tokens y cierre de sesion.
 * El refresh token se gestiona mediante cookies HttpOnly.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Value("${refresh.cookie.name}")
    private String refreshCookieName;

    private static final int REFRESH_COOKIE_MAX_AGE = 30 * 24 * 60 * 60; // 30 days

    /**
     * Registra un nuevo usuario en el sistema.
     * El rol asignado siempre es USER.
     *
     * @param request datos de registro del usuario
     * @return ResponseEntity con mensaje de exito (HTTP 200)
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok("User registered successfully");
    }

    /**
     * Inicia sesion de un usuario.
     * Genera un access token y un refresh token. El refresh token se almacena en una cookie HttpOnly.
     *
     * @param request  datos de inicio de sesion
     * @param response respuesta HTTP para establecer la cookie
     * @return ResponseEntity con el access token (HTTP 200)
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        AuthResponse authResponse = authService.login(request);
        setRefreshTokenCookie(response, authResponse.refreshToken());
        return ResponseEntity.ok(new AuthResponse(authResponse.accessToken(), null));
    }

    /**
     * Refresca el access token usando el refresh token almacenado en la cookie.
     * Aplica rotacion de refresh tokens: genera uno nuevo y lo almacena en la cookie.
     *
     * @param request  solicitud HTTP para leer la cookie
     * @param response respuesta HTTP para establecer la nueva cookie
     * @return ResponseEntity con el nuevo access token (HTTP 200), o 401 si no hay cookie
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshTokenFromCookie(request);
        if (refreshToken == null) {
            return ResponseEntity.status(401).build();
        }
        AuthResponse authResponse = authService.refresh(refreshToken);
        setRefreshTokenCookie(response, authResponse.refreshToken());
        return ResponseEntity.ok(new AuthResponse(authResponse.accessToken(), null));
    }

    /**
     * Cierra la sesion del usuario revocando el refresh token.
     * Elimina la cookie de refresh token.
     *
     * @param request  solicitud HTTP para leer la cookie
     * @param response respuesta HTTP para eliminar la cookie
     * @return ResponseEntity con mensaje de exito (HTTP 200)
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshTokenFromCookie(request);
        if (refreshToken != null) {
            authService.logout(refreshToken);
        }
        clearRefreshTokenCookie(response);
        return ResponseEntity.ok("Logged out successfully");
    }

    /**
     * Establece la cookie HttpOnly con el refresh token.
     *
     * @param response     respuesta HTTP
     * @param refreshToken valor del refresh token
     */
    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie(refreshCookieName, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Set to true in production with HTTPS
        cookie.setPath("/auth");
        cookie.setMaxAge(REFRESH_COOKIE_MAX_AGE);
        response.addCookie(cookie);
    }

    /**
     * Elimina la cookie de refresh token estableciendo su maxAge en 0.
     *
     * @param response respuesta HTTP
     */
    private void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(refreshCookieName, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/auth");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    /**
     * Extrae el refresh token de las cookies de la solicitud.
     *
     * @param request solicitud HTTP
     * @return valor del refresh token, o null si no existe
     */
    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (refreshCookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
