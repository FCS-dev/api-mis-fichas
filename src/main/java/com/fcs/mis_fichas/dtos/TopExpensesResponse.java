package com.fcs.mis_fichas.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "DTO con el top 3 de categorías y subcategorías con más gasto")
public record TopExpensesResponse(
        @Schema(description = "Top 3 categorías con más gasto")
        List<TopExpenseEntry> topCategories,

        @Schema(description = "Top 3 subcategorías con más gasto")
        List<TopExpenseEntry> topSubcategories
) {
}
