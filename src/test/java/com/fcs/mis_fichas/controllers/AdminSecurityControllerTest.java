package com.fcs.mis_fichas.controllers;

import com.fcs.mis_fichas.config.RateLimitInterceptor;
import com.fcs.mis_fichas.dtos.UnblockRequest;
import com.fcs.mis_fichas.repositories.RefreshTokenRepository;
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
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminSecurityController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = com.fcs.mis_fichas.config.JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
class AdminSecurityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BruteForceService bruteForceService;

    @MockBean
    private RefreshTokenRepository refreshTokenRepository;

    @MockBean
    private RateLimitInterceptor rateLimitInterceptor;

    @Test
    void getBlockedAccounts_shouldReturn200_withPaginationAndDefaultSort() throws Exception {
        BruteForceService.BlockedAccount blocked = new BruteForceService.BlockedAccount(
                "user@example.com", LocalDateTime.of(2026, 9, 16, 15, 30, 0));

        when(bruteForceService.getBlockedAccounts()).thenReturn(List.of(blocked));
        when(bruteForceService.sortBlockedAccounts(any(), anyString(), anyBoolean())).thenReturn(List.of(blocked));

        mockMvc.perform(get("/admin/security/brute-force/blocked"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].email").value("user@example.com"))
                .andExpect(jsonPath("$.data.content[0].lockedUntil").value("2026-09-16T15:30:00"))
                .andExpect(jsonPath("$.data.pagination.pageSize").value(20))
                .andExpect(jsonPath("$.data.pagination.totalElements").value(1));
    }

    @Test
    void getBlockedAccounts_shouldReturnEmpty_whenNoBlockedAccounts() throws Exception {
        when(bruteForceService.getBlockedAccounts()).thenReturn(List.of());
        when(bruteForceService.sortBlockedAccounts(any(), anyString(), anyBoolean())).thenReturn(List.of());

        mockMvc.perform(get("/admin/security/brute-force/blocked"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isEmpty())
                .andExpect(jsonPath("$.data.pagination.totalElements").value(0))
                .andExpect(jsonPath("$.data.pagination.totalPages").value(0));
    }

    @Test
    void getBlockedAccounts_shouldApplyCustomPageAndSize() throws Exception {
        BruteForceService.BlockedAccount blocked = new BruteForceService.BlockedAccount(
                "a@example.com", LocalDateTime.now().plusMinutes(15));

        when(bruteForceService.getBlockedAccounts()).thenReturn(List.of(blocked));
        when(bruteForceService.sortBlockedAccounts(any(), anyString(), anyBoolean())).thenReturn(List.of(blocked));

        mockMvc.perform(get("/admin/security/brute-force/blocked?page=0&size=10&sort=lockedUntil,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pagination.pageSize").value(10))
                .andExpect(jsonPath("$.data.pagination.sort").value("lockedUntil,desc"));
    }

    @Test
    void unblock_shouldReturn200_whenAccountWasBlocked() throws Exception {
        UnblockRequest request = new UnblockRequest("blocked@example.com");
        when(bruteForceService.unblock("blocked@example.com")).thenReturn(true);

        mockMvc.perform(post("/admin/security/brute-force/unblock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Cuenta desbloqueada correctamente"));

        verify(bruteForceService).unblock("blocked@example.com");
    }

    @Test
    void unblock_shouldReturn200_whenAccountWasNotBlocked() throws Exception {
        UnblockRequest request = new UnblockRequest("not-blocked@example.com");
        when(bruteForceService.unblock("not-blocked@example.com")).thenReturn(false);

        mockMvc.perform(post("/admin/security/brute-force/unblock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("La cuenta no estaba bloqueada"));
    }

    @Test
    void unblock_shouldReturn400_whenEmailIsBlank() throws Exception {
        UnblockRequest request = new UnblockRequest("  ");

        mockMvc.perform(post("/admin/security/brute-force/unblock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cleanupRefreshTokens_shouldReturn200_withDeletedCount() throws Exception {
        when(refreshTokenRepository.deleteByRevokedAtIsNotNullAndExpiresAtBefore(any(LocalDateTime.class))).thenReturn(42L);

        mockMvc.perform(post("/admin/security/refresh-tokens/cleanup"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.deletedCount").value(42));
    }

    @Test
    void cleanupRefreshTokens_shouldReturn200_withZeroDeletedCount() throws Exception {
        when(refreshTokenRepository.deleteByRevokedAtIsNotNullAndExpiresAtBefore(any(LocalDateTime.class))).thenReturn(0L);

        mockMvc.perform(post("/admin/security/refresh-tokens/cleanup"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.deletedCount").value(0));
    }
}
