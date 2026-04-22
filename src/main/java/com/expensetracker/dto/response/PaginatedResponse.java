package com.expensetracker.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * PaginatedResponse — standardized paginated API response wrapper.
 *
 * Structure:
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
 * This DTO wraps the list of results and the pagination metadata.
 * Use this for ALL paginated endpoints.
 *
 * @param <T> the type of data items in the list
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Paginated response wrapper")
public class PaginatedResponse<T> {

    @com.fasterxml.jackson.annotation.JsonProperty("data")
    @Schema(description = "List of data items for the current page")
    private List<T> data;

    @com.fasterxml.jackson.annotation.JsonProperty("pagination")
    @Schema(description = "Pagination metadata")
    private PaginationMetadata pagination;
}
