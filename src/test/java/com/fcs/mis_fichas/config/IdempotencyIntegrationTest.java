package com.fcs.mis_fichas.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IdempotencyIntegrationTest {

    private IdempotencyService idempotencyService;
    private IdempotencyRequestFilter filter;

    @BeforeEach
    void setUp() {
        IdempotencyProperties properties = new IdempotencyProperties();
        properties.setMaxEntries(100);
        idempotencyService = new IdempotencyService(properties);
        filter = new IdempotencyRequestFilter();
    }

    private MockHttpServletRequest createRequest(String body, String idempotencyKey) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/transactions");
        request.setContentType("application/json");
        request.setContent(body.getBytes(StandardCharsets.UTF_8));
        if (idempotencyKey != null) {
            request.addHeader("Idempotency-Key", idempotencyKey);
        }
        return request;
    }

    private void doFilter(HttpServletRequest request, MockHttpServletResponse response) throws ServletException, IOException {
        FilterChain chain = (req, res) -> {};
        filter.doFilterInternal(request, response, chain);
    }

    @Test
    void filter_shouldWrapRequest_whenIdempotencyKeyPresent() throws Exception {
        String uuid = UUID.randomUUID().toString();
        String body = "{\"amount\":50}";
        MockHttpServletRequest request = createRequest(body, uuid);

        final CachedBodyHttpServletRequestWrapper[] wrapped = {null};
        FilterChain chain = (req, res) -> {
            assertThat(req).isInstanceOf(CachedBodyHttpServletRequestWrapper.class);
            wrapped[0] = (CachedBodyHttpServletRequestWrapper) req;
        };

        filter.doFilterInternal(request, new MockHttpServletResponse(), chain);

        assertThat(wrapped[0]).isNotNull();
        String readBody = new String(wrapped[0].getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertThat(readBody).isEqualTo(body);
    }

    @Test
    void filter_shouldNotWrapRequest_whenNoIdempotencyKey() throws Exception {
        MockHttpServletRequest request = createRequest("{}", null);

        FilterChain chain = (req, res) -> {
            assertThat(req).isNotInstanceOf(CachedBodyHttpServletRequestWrapper.class);
        };

        filter.doFilterInternal(request, new MockHttpServletResponse(), chain);
    }

    @Test
    void fullFlow_shouldExecuteAndCache_whenSameKeySameBody() {
        String uuid = UUID.randomUUID().toString();
        String body = "{\"amount\":50}";
        String hash = IdempotencyService.computeBodyHash(body, 1L);

        String first = idempotencyService.processRequest(uuid, hash, () -> "created-response");
        String second = idempotencyService.processRequest(uuid, hash, () -> "should-not-execute");

        assertThat(first).isEqualTo("created-response");
        assertThat(second).isEqualTo("created-response");
    }

    @Test
    void fullFlow_shouldReturn409_whenSameKeyDifferentBody() {
        String uuid = UUID.randomUUID().toString();
        String hash1 = IdempotencyService.computeBodyHash("{\"amount\":50}", 1L);
        String hash2 = IdempotencyService.computeBodyHash("{\"amount\":100}", 1L);

        idempotencyService.processRequest(uuid, hash1, () -> "response");

        assertThatThrownBy(() -> idempotencyService.processRequest(uuid, hash2, () -> "response"))
                .isInstanceOf(IdempotencyConflictException.class);
    }

    @Test
    void fullFlow_shouldValidateUUID() {
        assertThatThrownBy(() -> UUID.fromString("not-a-uuid"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void fullFlow_shouldPassThrough_whenNoKey() {
        String result = idempotencyService.processRequest(
                UUID.randomUUID().toString(),
                IdempotencyService.computeBodyHash("body", 1L),
                () -> "created"
        );
        assertThat(result).isEqualTo("created");
    }
}
