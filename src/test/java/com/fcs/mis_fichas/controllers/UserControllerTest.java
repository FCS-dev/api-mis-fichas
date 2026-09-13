package com.fcs.mis_fichas.controllers;

import com.fcs.mis_fichas.config.RateLimitInterceptor;
import com.fcs.mis_fichas.dtos.UserResponse;
import com.fcs.mis_fichas.dtos.UserUpdateRequest;
import com.fcs.mis_fichas.enums.Role;
import com.fcs.mis_fichas.enums.Status;
import com.fcs.mis_fichas.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = com.fcs.mis_fichas.config.JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private RateLimitInterceptor rateLimitInterceptor;

    private static UserResponse sampleResponse(Long id, String email, Role role, Status status) {
        return new UserResponse(id, email, "Test User", role, status,
                LocalDateTime.now(), LocalDateTime.now(), null);
    }

    @Test
    void findById_shouldReturn200_whenUserExists() throws Exception {
        UserResponse response = sampleResponse(1L, "user@example.com", Role.USER, Status.ACTIVE);

        when(userService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/admin/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.email").value("user@example.com"));
    }

    @Test
    void findAll_shouldReturn200_withPaginationAndFilters() throws Exception {
        UserResponse response = sampleResponse(1L, "admin@example.com", Role.ADMIN, Status.ACTIVE);
        Page<UserResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 20, Sort.by("name").ascending()), 1);

        when(userService.findAll(eq(Role.ADMIN), eq(Status.ACTIVE), any())).thenReturn(page);

        mockMvc.perform(get("/admin/users?role=ADMIN&status=ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].role").value("ADMIN"))
                .andExpect(jsonPath("$.data.content[0].status").value("ACTIVE"));

        verify(userService).findAll(eq(Role.ADMIN), eq(Status.ACTIVE), any());
    }

    @Test
    void findAll_shouldReturn200_withoutFilters() throws Exception {
        UserResponse response = sampleResponse(1L, "user@example.com", Role.USER, Status.ACTIVE);
        Page<UserResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 20, Sort.by("name").ascending()), 1);

        when(userService.findAll(isNull(), isNull(), any())).thenReturn(page);

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].email").value("user@example.com"));

        verify(userService).findAll(isNull(), isNull(), any());
    }

    @Test
    void update_shouldReturn200_whenRequestValid() throws Exception {
        UserUpdateRequest request = new UserUpdateRequest("Updated Name", "updated@example.com", Role.USER, Status.BLOCKED);
        UserResponse response = sampleResponse(1L, "updated@example.com", Role.USER, Status.BLOCKED);
        response = new UserResponse(1L, "updated@example.com", "Updated Name", Role.USER, Status.BLOCKED,
                response.createdAt(), response.updatedAt(), response.deletedAt());

        when(userService.update(eq(1L), any(UserUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/admin/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Updated Name"))
                .andExpect(jsonPath("$.data.status").value("BLOCKED"));

        verify(userService).update(eq(1L), any(UserUpdateRequest.class));
    }

    @Test
    void delete_shouldReturn200_whenIdExists() throws Exception {
        mockMvc.perform(delete("/admin/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User deleted successfully"));

        verify(userService).delete(1L);
    }
}
