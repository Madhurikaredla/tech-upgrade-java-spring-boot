package com.expensetracker.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PaginationMetadata — metadata for paginated API responses.
 *
 * Standard pagination structure:
 * {
 *   "data": [...],
 *   "pagination": {
 *     "current_page": 2,
 *     "page_size": 20,
 *     "total_items": 145,
 *     "total_pages": 8,
 *     "has_next": true,
 *     "links": {
 *       "next": "/api/v1/items?page=3&size=20",
 *       "prev": "/api/v1/items?page=1&size=20"
 *     }
 *   }
 * }
 *
 * This DTO represents the "pagination" section of the response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Pagination metadata for list responses")
public class PaginationMetadata {

    @JsonProperty("current_page")
    @Schema(description = "Current page number (1-indexed)", example = "2")
    private int currentPage;

    @JsonProperty("page_size")
    @Schema(description = "Number of items per page", example = "20")
    private int pageSize;

    @JsonProperty("total_items")
    @Schema(description = "Total number of items across all pages", example = "145")
    private long totalItems;

    @JsonProperty("total_pages")
    @Schema(description = "Total number of pages", example = "8")
    private int totalPages;

    @JsonProperty("has_next")
    @Schema(description = "True if there is a next page available", example = "true")
    private boolean hasNext;

    @JsonProperty("links")
    @Schema(description = "Navigation links to next and previous pages")
    private PaginationLinks links;
}
