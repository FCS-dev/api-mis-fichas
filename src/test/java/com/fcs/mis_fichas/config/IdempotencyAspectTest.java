package com.fcs.mis_fichas.config;

import com.fcs.mis_fichas.services.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdempotencyAspectTest {

    @Mock
    private IdempotencyService idempotencyService;

    @Mock
    private JwtService jwtService;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private HttpServletRequest request;

    private IdempotencyAspect aspect;

    @BeforeEach
    void setUp() {
        aspect = new IdempotencyAspect(idempotencyService, jwtService);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, null));
    }

    @Test
    void shouldPassThrough_whenNoIdempotencyKeyHeader() throws Throwable {
        when(request.getHeader("Idempotency-Key")).thenReturn(null);
        when(joinPoint.proceed()).thenReturn("result");

        Object result = aspect.around(joinPoint);

        assertThat(result).isEqualTo("result");
        verify(joinPoint).proceed();
        verify(idempotencyService, never()).processRequest(any(), any(), any());
    }

    @Test
    void shouldPassThrough_whenHeaderIsBlank() throws Throwable {
        when(request.getHeader("Idempotency-Key")).thenReturn("   ");
        when(joinPoint.proceed()).thenReturn("result");

        Object result = aspect.around(joinPoint);

        assertThat(result).isEqualTo("result");
        verify(idempotencyService, never()).processRequest(any(), any(), any());
    }

    @Test
    void shouldThrow409_whenKeyIsNotValidUUID() throws Throwable {
        when(request.getHeader("Idempotency-Key")).thenReturn("not-a-uuid");

        assertThatThrownBy(() -> aspect.around(joinPoint))
                .isInstanceOf(IdempotencyConflictException.class)
                .hasMessageContaining("valid UUID");
    }

    @Test
    void shouldDelegateToService_whenValidUUIDProvided() throws Throwable {
        String uuid = UUID.randomUUID().toString();
        when(request.getHeader("Idempotency-Key")).thenReturn(uuid);
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getReader()).thenReturn(new java.io.BufferedReader(new java.io.StringReader("{\"amount\":50}")));
        when(idempotencyService.processRequest(eq(uuid), anyString(), any())).thenReturn("cached");

        Object result = aspect.around(joinPoint);

        assertThat(result).isEqualTo("cached");
        verify(idempotencyService).processRequest(eq(uuid), anyString(), any());
    }

    @Test
    void shouldExtractUserIdFromJwtToken() throws Throwable {
        String uuid = UUID.randomUUID().toString();
        when(request.getHeader("Idempotency-Key")).thenReturn(uuid);
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(request.getReader()).thenReturn(new java.io.BufferedReader(new java.io.StringReader("{\"amount\":50}")));
        when(jwtService.extractUserId("valid-token")).thenReturn(42L);
        when(idempotencyService.processRequest(eq(uuid), anyString(), any())).thenReturn("ok");

        aspect.around(joinPoint);

        verify(jwtService).extractUserId("valid-token");
    }

    @Test
    void shouldUseZero_whenNoAuthorizationHeader() throws Throwable {
        String uuid = UUID.randomUUID().toString();
        when(request.getHeader("Idempotency-Key")).thenReturn(uuid);
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getReader()).thenReturn(new java.io.BufferedReader(new java.io.StringReader("")));
        when(idempotencyService.processRequest(eq(uuid), anyString(), any())).thenReturn("ok");

        aspect.around(joinPoint);

        verify(jwtService, never()).extractUserId(any());
    }
}
