package com.fcs.mis_fichas.controllers;

import com.fcs.mis_fichas.dtos.TransactionRequest;
import com.fcs.mis_fichas.dtos.TransactionResponse;
import com.fcs.mis_fichas.services.TransactionService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TransactionController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = com.fcs.mis_fichas.config.JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    private static TransactionResponse sampleResponse(Long id) {
        return new TransactionResponse(id, 1L, "user@example.com", 1L, "Food",
                1L, "Groceries", new BigDecimal("50.00"), "Lunch", LocalDate.now(),
                null, null, null);
    }

    @Test
    void create_shouldReturn201_whenRequestValid() throws Exception {
        TransactionRequest request = new TransactionRequest(null, 1L, 1L, new BigDecimal("50.00"), "Lunch", LocalDate.now());
        TransactionResponse response = sampleResponse(1L);

        when(transactionService.create(any(TransactionRequest.class))).thenReturn(response);

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.amount").value(50.00))
                .andExpect(jsonPath("$.data.description").value("Lunch"));

        verify(transactionService).create(any(TransactionRequest.class));
    }

    @Test
    void update_shouldReturn200_whenRequestValid() throws Exception {
        TransactionRequest request = new TransactionRequest(null, 1L, 1L, new BigDecimal("75.00"), "Dinner", LocalDate.now());
        TransactionResponse response = sampleResponse(1L);
        response = new TransactionResponse(1L, 1L, "user@example.com", 1L, "Food", 1L, "Groceries",
                new BigDecimal("75.00"), "Dinner", LocalDate.now(), null, null, null);

        when(transactionService.update(eq(1L), any(TransactionRequest.class))).thenReturn(response);

        mockMvc.perform(put("/transactions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.amount").value(75.00))
                .andExpect(jsonPath("$.data.description").value("Dinner"));

        verify(transactionService).update(eq(1L), any(TransactionRequest.class));
    }

    @Test
    void delete_shouldReturn200_whenIdExists() throws Exception {
        mockMvc.perform(delete("/transactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Transaction deleted successfully"));

        verify(transactionService).delete(1L);
    }

    @Test
    void findById_shouldReturn200_whenTransactionExists() throws Exception {
        TransactionResponse response = sampleResponse(1L);

        when(transactionService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/transactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.amount").value(50.00));
    }

    @Test
    void findAll_shouldReturn200_withFilters() throws Exception {
        TransactionResponse response = sampleResponse(1L);
        Page<TransactionResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 20, Sort.by("transactionDate").descending()), 1);

        when(transactionService.findAll(eq(1L), eq(2L), eq(3L), eq(LocalDate.of(2024, 1, 15)), eq(LocalDate.of(2024, 1, 1)), eq(LocalDate.of(2024, 1, 31)), any())).thenReturn(page);

        mockMvc.perform(get("/transactions?userId=1&categoryId=2&subcategoryId=3&date=2024-01-15&dateFrom=2024-01-01&dateTo=2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.pagination.sort").value("transactionDate: DESC"));

        verify(transactionService).findAll(eq(1L), eq(2L), eq(3L), eq(LocalDate.of(2024, 1, 15)), eq(LocalDate.of(2024, 1, 1)), eq(LocalDate.of(2024, 1, 31)), any());
    }

    @Test
    void findByCategory_shouldReturn200() throws Exception {
        TransactionResponse response = sampleResponse(1L);
        Page<TransactionResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 20, Sort.by("transactionDate").descending()), 1);

        when(transactionService.findAll(isNull(), eq(5L), isNull(), isNull(), isNull(), isNull(), any())).thenReturn(page);

        mockMvc.perform(get("/transactions/category/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].categoryId").value(1));
    }

    @Test
    void findBySubcategory_shouldReturn200() throws Exception {
        TransactionResponse response = sampleResponse(1L);
        Page<TransactionResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 20, Sort.by("transactionDate").descending()), 1);

        when(transactionService.findAll(isNull(), isNull(), eq(7L), isNull(), isNull(), isNull(), any())).thenReturn(page);

        mockMvc.perform(get("/transactions/subcategory/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].subcategoryId").value(1));
    }

    @Test
    void findByDate_shouldReturn200() throws Exception {
        TransactionResponse response = new TransactionResponse(1L, 1L, "user@example.com", 1L, "Food",
                1L, "Groceries", new BigDecimal("50.00"), "Lunch", LocalDate.of(2024, 6, 15),
                null, null, null);
        Page<TransactionResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 20, Sort.by("transactionDate").descending()), 1);

        when(transactionService.findAll(isNull(), isNull(), isNull(), eq(LocalDate.of(2024, 6, 15)), isNull(), isNull(), any())).thenReturn(page);

        mockMvc.perform(get("/transactions/date/2024-06-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].transactionDate").value("2024-06-15"));
    }

    @Test
    void findByDateRange_shouldReturn200() throws Exception {
        TransactionResponse response = sampleResponse(1L);
        Page<TransactionResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 20, Sort.by("transactionDate").descending()), 1);

        when(transactionService.findAll(isNull(), isNull(), isNull(), isNull(), eq(LocalDate.of(2024, 1, 1)), eq(LocalDate.of(2024, 1, 31)), any())).thenReturn(page);

        mockMvc.perform(get("/transactions/date-range?from=2024-01-01&to=2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(1));
    }

    @Test
    void findByDateRange_shouldReturn200_withOptionalUserId() throws Exception {
        TransactionResponse response = sampleResponse(1L);
        Page<TransactionResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 20, Sort.by("transactionDate").descending()), 1);

        when(transactionService.findAll(eq(2L), isNull(), isNull(), isNull(), eq(LocalDate.of(2024, 1, 1)), eq(LocalDate.of(2024, 1, 31)), any())).thenReturn(page);

        mockMvc.perform(get("/transactions/date-range?from=2024-01-01&to=2024-01-31&userId=2"))
                .andExpect(status().isOk());

        verify(transactionService).findAll(eq(2L), isNull(), isNull(), isNull(), eq(LocalDate.of(2024, 1, 1)), eq(LocalDate.of(2024, 1, 31)), any());
    }
}
