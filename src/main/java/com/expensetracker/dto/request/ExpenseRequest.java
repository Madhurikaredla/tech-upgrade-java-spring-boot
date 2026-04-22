package com.expensetracker.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
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
@Schema(description = "Request body for creating/updating an expense")
public class ExpenseRequest {
    @NotNull(message = "{validation.expense.amount.required}")
    @DecimalMin(value = "0.01", message = "{validation.expense.amount.min}")
    @Digits(integer = 15, fraction = 4, message = "{validation.expense.amount.digits}")
    @Schema(description = "Expense amount", example = "100.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal amount;

    @NotBlank(message = "{validation.expense.description.required}")
    @Size(max = 255, message = "{validation.expense.description.size}")
    @Schema(description = "Expense description", example = "Lunch at restaurant", requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;

    @NotNull(message = "{validation.expense.date.required}")
    @PastOrPresent(message = "{validation.expense.date.pastOrPresent}")
    @Schema(description = "Expense date (yyyy-MM-dd)", example = "2026-04-21", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate expenseDate;

    @NotNull(message = "{validation.expense.categories.required}")
    @Size(min = 1, message = "{validation.expense.categories.required}")
    @Schema(description = "List of category IDs for this expense", example = "[1,2]", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> categoryIds;
}
