package com.expensetracker.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CategoryRequest — request body for POST /api/v1/categories (create category).
 *
 * Validation rules:
 *   name : required, max 50 characters, must be unique within user's scope
 *
 * All validation messages reference i18n property keys.
 * The userId is never accepted from the request body — it is extracted from the JWT token.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for creating or updating a category")
public class CategoryRequest {

    /**
     * Category name (e.g., "Food", "Transport", "Entertainment").
     * Required. Max 50 characters.
     * Must be unique within the user's categories.
     */
    @NotBlank(message = "{validation.category.name.required}")
    @Size(max = 50, message = "{validation.category.name.size}")
    @Schema(description = "Category name", example = "Food", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    /**
     * Unique key for the category (e.g., "FOOD", "TRANSPORT").
     * Required. Max 50 characters.
     * Used for programmatic identification and i18n lookups.
     * Must be unique globally across all categories.
     * Should be UPPER_SNAKE_CASE by convention.
     */
    @NotBlank(message = "{validation.category.key.required}")
    @Size(max = 50, message = "{validation.category.key.size}")
    @Schema(description = "Category key (unique identifier)", example = "FOOD", requiredMode = Schema.RequiredMode.REQUIRED)
    private String categoryKey;
}
