package com.fcs.mis_fichas.controllers;

import com.fcs.mis_fichas.dtos.SubcategoryRequest;
import com.fcs.mis_fichas.dtos.SubcategoryResponse;
import com.fcs.mis_fichas.services.SubcategoryService;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SubcategoryController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = com.fcs.mis_fichas.config.JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
class SubcategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SubcategoryService subcategoryService;

    private static SubcategoryResponse sampleResponse(Long id, String name) {
        return new SubcategoryResponse(id, name, "Comments", false, 1L, "Food",
                1L, "user@example.com", LocalDateTime.now(), LocalDateTime.now(), null);
    }

    @Test
    void create_shouldReturn201_whenRequestValid() throws Exception {
        SubcategoryRequest request = new SubcategoryRequest("Groceries", 1L, "Comments");
        SubcategoryResponse response = sampleResponse(1L, "Groceries");

        when(subcategoryService.create(any(SubcategoryRequest.class))).thenReturn(response);

        mockMvc.perform(post("/subcategories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Groceries"))
                .andExpect(jsonPath("$.data.categoryId").value(1));

        verify(subcategoryService).create(any(SubcategoryRequest.class));
    }

    @Test
    void update_shouldReturn200_whenRequestValid() throws Exception {
        SubcategoryRequest request = new SubcategoryRequest("Updated", 1L, "Updated comments");
        SubcategoryResponse response = sampleResponse(1L, "Updated");

        when(subcategoryService.update(eq(1L), any(SubcategoryRequest.class))).thenReturn(response);

        mockMvc.perform(put("/subcategories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Updated"));

        verify(subcategoryService).update(eq(1L), any(SubcategoryRequest.class));
    }

    @Test
    void delete_shouldReturn200_whenIdExists() throws Exception {
        mockMvc.perform(delete("/subcategories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Subcategory deleted successfully"));

        verify(subcategoryService).delete(1L);
    }

    @Test
    void findById_shouldReturn200_whenSubcategoryExists() throws Exception {
        SubcategoryResponse response = sampleResponse(1L, "Groceries");

        when(subcategoryService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/subcategories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Groceries"));
    }

    @Test
    void findAll_shouldReturn200_withPaginationDefaults() throws Exception {
        SubcategoryResponse response = sampleResponse(1L, "Groceries");
        Page<SubcategoryResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 20, Sort.by("name").ascending()), 1);

        when(subcategoryService.findAll(any())).thenReturn(page);

        mockMvc.perform(get("/subcategories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("Groceries"))
                .andExpect(jsonPath("$.data.pagination.currentPage").value(0))
                .andExpect(jsonPath("$.data.pagination.pageSize").value(20));
    }
}
