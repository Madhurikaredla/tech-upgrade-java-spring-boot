package com.expensetracker.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Enum for sort direction in pageable endpoints.
 * Only ASC and DESC are allowed.
 */
@Schema(description = "Sort direction for pageable endpoints.")
public enum SortDirection {
    @Schema(description = "Ascending order", example = "ASC")
    ASC,
    @Schema(description = "Descending order", example = "DESC")
    DESC;

    public static boolean isValid(String value) {
        if (value == null) return false;
        return value.equalsIgnoreCase("ASC") || value.equalsIgnoreCase("DESC");
    }
}
