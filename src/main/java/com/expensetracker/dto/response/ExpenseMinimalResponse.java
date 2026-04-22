package com.expensetracker.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Minimal expense response for create/update endpoints")
public class ExpenseMinimalResponse {
    @Schema(description = "Expense ID", example = "1")
    private Long id;

    @Schema(description = "Expense amount", example = "100.00")
    private BigDecimal amount;

    @Schema(description = "Expense description", example = "Lunch at restaurant")
    private String description;

    @Schema(description = "Expense date (yyyy-MM-dd)", example = "2026-04-21")
    private LocalDate expenseDate;

    @Schema(description = "List of categories for this expense")
    private List<CategoryMinimalResponse> categories;
}
