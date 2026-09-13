package com.fcs.mis_fichas.controllers;

import com.fcs.mis_fichas.config.RateLimitInterceptor;
import com.fcs.mis_fichas.dtos.AuthResponse;
import com.fcs.mis_fichas.dtos.LoginRequest;
import com.fcs.mis_fichas.dtos.RegisterRequest;
import com.fcs.mis_fichas.services.AuthService;
import com.fcs.mis_fichas.services.BruteForceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.Cookie;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = com.fcs.mis_fichas.config.JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private BruteForceService bruteForceService;

    @MockBean
    private RateLimitInterceptor rateLimitInterceptor;

    @Test
    void register_shouldReturn200_whenRequestValid() throws Exception {
        RegisterRequest request = new RegisterRequest("user@example.com", "password123", "User");

        when(authService.register(any(RegisterRequest.class))).thenReturn(null);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    void login_shouldReturn200_andSetRefreshCookie_whenCredentialsValid() throws Exception {
        LoginRequest request = new LoginRequest("user@example.com", "password123");
        AuthResponse authResponse = new AuthResponse("access-token", "refresh-token");

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").doesNotExist())
                .andExpect(cookie().exists("refresh_token"))
                .andExpect(cookie().value("refresh_token", "refresh-token"))
                .andExpect(cookie().httpOnly("refresh_token", true));
    }

    @Test
    void refresh_shouldReturn401_whenNoCookiePresent() throws Exception {
        mockMvc.perform(post("/auth/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Refresh token not found"));

        verify(authService, never()).refresh(any());
    }

    @Test
    void refresh_shouldReturn200_andRotateCookie_whenCookiePresent() throws Exception {
        AuthResponse authResponse = new AuthResponse("new-access-token", "new-refresh-token");

        when(authService.refresh("old-refresh-token")).thenReturn(authResponse);

        mockMvc.perform(post("/auth/refresh")
                        .cookie(new Cookie("refresh_token", "old-refresh-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
                .andExpect(cookie().exists("refresh_token"))
                .andExpect(cookie().value("refresh_token", "new-refresh-token"));

        verify(authService).refresh("old-refresh-token");
    }

    @Test
    void logout_shouldReturn200_andClearCookie_whenCookiePresent() throws Exception {
        mockMvc.perform(post("/auth/logout")
                        .cookie(new Cookie("refresh_token", "refresh-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Logged out successfully"))
                .andExpect(cookie().exists("refresh_token"))
                .andExpect(cookie().maxAge("refresh_token", 0));

        verify(authService).logout("refresh-token");
    }

    @Test
    void logout_shouldReturn200_andClearCookie_evenWhenNoCookiePresent() throws Exception {
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Logged out successfully"))
                .andExpect(cookie().exists("refresh_token"))
                .andExpect(cookie().maxAge("refresh_token", 0));

        verify(authService, never()).logout(any());
    }

    @Test
    void login_shouldReturn429_whenAccountBlocked() throws Exception {
        LoginRequest request = new LoginRequest("user@example.com", "password123");

        when(bruteForceService.isBlocked("user@example.com")).thenReturn(true);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Account temporarily locked due to too many failed attempts. Try again later."));

        verify(authService, never()).login(any());
    }

    @Test
    void login_shouldReturn401_whenBadCredentials() throws Exception {
        LoginRequest request = new LoginRequest("user@example.com", "wrongpassword");

        when(bruteForceService.isBlocked("user@example.com")).thenReturn(false);
        when(authService.login(any(LoginRequest.class))).thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid credentials"));

        verify(bruteForceService).recordFailedAttempt("user@example.com");
        verify(authService).login(any());
    }

    @Test
    void login_shouldResetAttempts_whenCredentialsValid() throws Exception {
        LoginRequest request = new LoginRequest("user@example.com", "password123");
        AuthResponse authResponse = new AuthResponse("access-token", "refresh-token");

        when(bruteForceService.isBlocked("user@example.com")).thenReturn(false);
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"));

        verify(bruteForceService).resetAttempts("user@example.com");
        verify(bruteForceService, never()).recordFailedAttempt(any());
    }
}
