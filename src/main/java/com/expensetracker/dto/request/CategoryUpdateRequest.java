package com.expensetracker.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CategoryUpdateRequest — request body for PUT /api/v1/categories/{id} (update category).
 *
 * Only the name can be updated. Category key is immutable.
 *
 * All validation messages reference i18n property keys.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for updating a category (name only)")
public class CategoryUpdateRequest {

    /**
     * Category name (e.g., "Food", "Transport", "Entertainment").
     * Required. Max 50 characters.
     * Must be unique within the user's categories.
     */
    @NotBlank(message = "{validation.category.name.required}")
    @Size(max = 50, message = "{validation.category.name.size}")
    @Schema(description = "Category name", example = "Food", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;
}
