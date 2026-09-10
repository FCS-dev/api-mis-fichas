package com.fcs.mis_fichas.controllers;

import com.fcs.mis_fichas.dtos.ApiResponse;
import com.fcs.mis_fichas.dtos.AuthResponse;
import com.fcs.mis_fichas.dtos.LoginRequest;
import com.fcs.mis_fichas.dtos.RegisterRequest;
import com.fcs.mis_fichas.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * Controlador REST para la autenticación.
 * Proporciona endpoints públicos para registro, inicio de sesión,
 * refresco de tokens y cierre de sesión.
 * El refresh token se gestiona mediante cookies HttpOnly.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints públicos para registro, login, refresco de tokens y logout")
public class AuthController {

    private final AuthService authService;
    private static final String REFRESH_COOKIE_NAME = "refresh_token";

    private static final int REFRESH_COOKIE_MAX_AGE = 30 * 24 * 60 * 60; // 30 days

    /**
     * Registra un nuevo usuario en el sistema.
     * El rol asignado siempre es USER.
     *
     * @param request     datos de registro del usuario
     * @param httpRequest solicitud HTTP
     * @return ResponseEntity con ApiResponse de mensaje de éxito (HTTP 200)
     */
    @PostMapping("/register")
    @Operation(summary = "Registrar un nuevo usuario", description = "Crea una cuenta con rol USER. Devuelve un mensaje de éxito.")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        authService.register(request);
        ApiResponse<String> apiResponse = new ApiResponse<>(
                true, HttpStatus.OK.value(), "User registered successfully", null,
                LocalDateTime.now(), httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Inicia sesión de un usuario.
     * Genera un access token y un refresh token. El refresh token se almacena en una cookie HttpOnly.
     *
     * @param request     datos de inicio de sesión
     * @param response    respuesta HTTP para establecer la cookie
     * @param httpRequest solicitud HTTP
     * @return ResponseEntity con ApiResponse del access token (HTTP 200)
     */
    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica al usuario y devuelve un access token. El refresh token se envía en una cookie HttpOnly.")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response, HttpServletRequest httpRequest) {
        AuthResponse authResponse = authService.login(request);
        setRefreshTokenCookie(response, authResponse.refreshToken(), httpRequest);
        ApiResponse<AuthResponse> apiResponse = new ApiResponse<>(
                true, HttpStatus.OK.value(), null, new AuthResponse(authResponse.accessToken(), null),
                LocalDateTime.now(), httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Refresca el access token usando el refresh token almacenado en la cookie.
     * Aplica rotación de refresh tokens: genera uno nuevo y lo almacena en la cookie.
     *
     * @param request     solicitud HTTP para leer la cookie
     * @param response    respuesta HTTP para establecer la nueva cookie
     * @param httpRequest solicitud HTTP
     * @return ResponseEntity con ApiResponse del nuevo access token (HTTP 200), o 401 si no hay cookie
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refrescar access token", description = "Genera un nuevo access token usando el refresh token de la cookie. Aplica rotación de refresh tokens.")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(HttpServletRequest request, HttpServletResponse response, HttpServletRequest httpRequest) {
        String refreshToken = extractRefreshTokenFromCookie(request);
        if (refreshToken == null) {
            ApiResponse<AuthResponse> apiResponse = new ApiResponse<>(
                    false, HttpStatus.UNAUTHORIZED.value(), "Refresh token not found", null,
                    LocalDateTime.now(), httpRequest.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse);
        }
        AuthResponse authResponse = authService.refresh(refreshToken);
        setRefreshTokenCookie(response, authResponse.refreshToken(), httpRequest);
        ApiResponse<AuthResponse> apiResponse = new ApiResponse<>(
                true, HttpStatus.OK.value(), null, new AuthResponse(authResponse.accessToken(), null),
                LocalDateTime.now(), httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Cierra la sesión del usuario revocando el refresh token.
     * Elimina la cookie de refresh token.
     *
     * @param request  solicitud HTTP para leer la cookie
     * @param response respuesta HTTP para eliminar la cookie
     * @return ResponseEntity con ApiResponse de mensaje de éxito (HTTP 200)
     */
    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión", description = "Revoca el refresh token y elimina la cookie HttpOnly.")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request, HttpServletResponse response, HttpServletRequest httpRequest) {
        String refreshToken = extractRefreshTokenFromCookie(request);
        if (refreshToken != null) {
            authService.logout(refreshToken);
        }
        clearRefreshTokenCookie(response);
        ApiResponse<String> apiResponse = new ApiResponse<>(
                true, HttpStatus.OK.value(), "Logged out successfully", null,
                LocalDateTime.now(), httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Establece la cookie HttpOnly con el refresh token.
     * Usa SameSite=None; Secure cuando la conexión es HTTPS (producción).
     * Usa SameSite=Lax cuando la conexión es HTTP (desarrollo local).
     *
     * @param response     respuesta HTTP
     * @param refreshToken valor del refresh token
     * @param request      solicitud HTTP para verificar si la conexión es segura
     */
    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken, HttpServletRequest request) {
        boolean isSecure = request.isSecure();
        boolean isLocalhost = "localhost".equals(request.getServerName());
        boolean useSameSiteNone = isSecure || isLocalhost;
        String sameSite = useSameSiteNone ? "None" : "Lax";
        String secureAttr = useSameSiteNone ? "; Secure" : "";
        String cookie = String.format(
                "%s=%s; Path=/api/v1/auth; MaxAge=%d; HttpOnly; SameSite=%s%s",
                REFRESH_COOKIE_NAME, refreshToken, REFRESH_COOKIE_MAX_AGE, sameSite, secureAttr
        );
        response.addHeader("Set-Cookie", cookie);
    }

    /**
     * Elimina la cookie de refresh token estableciendo su maxAge en 0.
     *
     * @param response respuesta HTTP
     */
    private void clearRefreshTokenCookie(HttpServletResponse response) {
        String cookie = String.format(
                "%s=; Path=/api/v1/auth; MaxAge=0; HttpOnly; SameSite=None; Secure",
                REFRESH_COOKIE_NAME
        );
        response.addHeader("Set-Cookie", cookie);
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
                if (REFRESH_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
