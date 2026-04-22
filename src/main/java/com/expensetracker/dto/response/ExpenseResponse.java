package com.expensetracker.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Expense response DTO")
public class ExpenseResponse {
    @Schema(description = "Expense ID", example = "1")
    private Long id;

    @Schema(description = "Expense amount", example = "100.00")
    private BigDecimal amount;

    @Schema(description = "Expense description", example = "Lunch at restaurant")
    private String description;

    @Schema(description = "Expense date (yyyy-MM-dd)", example = "2026-04-21")
    private LocalDate expenseDate;

    @Schema(description = "User ID", example = "42")
    private Long userId;

    @Schema(description = "List of categories for this expense")
    private List<CategoryResponse> categories;

    @Schema(description = "Created at timestamp", example = "2026-04-21T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Updated at timestamp", example = "2026-04-21T10:30:00")
    private LocalDateTime updatedAt;
}
