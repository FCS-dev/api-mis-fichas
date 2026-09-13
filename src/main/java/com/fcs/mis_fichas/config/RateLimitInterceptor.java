package com.fcs.mis_fichas.config;

import com.fcs.mis_fichas.dtos.ApiResponse;
import com.fcs.mis_fichas.services.IpRateLimitService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final IpRateLimitService ipRateLimitService;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String ip = resolveIp(request);

        if (!ipRateLimitService.isAllowed(ip)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            ApiResponse<Void> apiResponse = new ApiResponse<>(
                    false,
                    HttpStatus.TOO_MANY_REQUESTS.value(),
                    "Too many requests. Try again later.",
                    null,
                    LocalDateTime.now(),
                    request.getRequestURI()
            );
            response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
            return false;
        }

        return true;
    }

    private String resolveIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
