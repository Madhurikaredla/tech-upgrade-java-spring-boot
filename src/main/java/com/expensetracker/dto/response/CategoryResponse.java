package com.expensetracker.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * CategoryResponse — response DTO for category-related endpoints.
 *
 * Returned by:
 *   - GET /api/v1/categories (list)
 *   - GET /api/v1/categories/{id} (get one)
 *   - POST /api/v1/categories (create)
 *   - PUT /api/v1/categories/{id} (update)
 *
 * NEVER includes internal fields like isDeleted.
 * All fields annotated with @Schema for Swagger documentation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Category response")
public class CategoryResponse {

    @Schema(description = "Category ID", example = "1")
    private Long id;

    @Schema(description = "Category name", example = "Food")
    private String name;

    @Schema(description = "Category key (unique identifier)", example = "FOOD")
    private String categoryKey;

    @Schema(description = "User ID (null for system categories)", example = "42")
    private Long userId;

    @Schema(description = "True if this is a system-level category (available to all users)", example = "false")
    private Boolean isSystemCategory;

    @Schema(description = "Timestamp of category creation", example = "2026-04-21T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp of last update", example = "2026-04-21T10:30:00")
    private LocalDateTime updatedAt;
}
