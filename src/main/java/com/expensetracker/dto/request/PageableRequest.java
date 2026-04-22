package com.expensetracker.dto.request;

import com.expensetracker.validation.ValidSortDirection;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageableRequest {
    @Min(value = 0, message = "{validation.pageable.page.min}")
    @Parameter(description = "0-based page index", example = "0")
    private int page = 0;

    @Min(value = 1, message = "{validation.pageable.size.min}")
    @Parameter(description = "Page size (items per page)", example = "20")
    private int size = 20;

    @Size(max = 50, message = "{validation.pageable.sort.size}")
    @Parameter(description = "Sort by field name", example = "id")
    private String sort = "id";

    @ValidSortDirection
    @Parameter(description = "Sort direction (ASC or DESC)", example = "DESC")
    private String direction = "DESC";
}
