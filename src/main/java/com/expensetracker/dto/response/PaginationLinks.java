package com.expensetracker.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PaginationLinks — contains navigation links for paginated responses.
 *
 * Provides next and previous page URLs for easy navigation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Navigation links for paginated results")
public class PaginationLinks {

    @JsonProperty("next")
    @Schema(description = "URL to the next page (null if on last page)", example = "/api/v1/categories?page=3&size=20")
    private String next;

    @JsonProperty("prev")
    @Schema(description = "URL to the previous page (null if on first page)", example = "/api/v1/categories?page=1&size=20")
    private String prev;
}
