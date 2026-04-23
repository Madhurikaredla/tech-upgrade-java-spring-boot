package com.expensetracker.controller;

import com.expensetracker.dto.request.CategoryRequest;
import com.expensetracker.dto.response.ApiResponse;
import com.expensetracker.dto.response.CategoryResponse;
import com.expensetracker.dto.response.PaginatedResponse;
import com.expensetracker.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.expensetracker.util.AppLogger;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import com.expensetracker.dto.request.PageableRequest;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * CategoryController — handles category management endpoints.
 *
 * Base path : /api/v1/categories
 * Protected : all endpoints require valid JWT token (Authorization: Bearer <token>)
 *
 * Controller rules enforced:
 *  - No business logic — all processing delegated to CategoryService.
 *  - No @Transactional — transaction boundary belongs in the service layer.
 *  - No direct repository access.
 *  - All responses wrapped in ApiResponse<T>.
 *  - All request DTOs validated with @Valid.
 *  - Constructor injection only.
 *  - Accept-Language header supported on all endpoints for i18n.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Category management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {

    private final CategoryService categoryService;
    private final MessageSource messageSource;

    // ── POST /api/v1/categories ───────────────────────────────────────────────

    /**
     * Creates a new category for the authenticated user.
     *
     * HTTP 201 Created on success.
     */
    @PostMapping
    @Operation(
            summary = "Create a new category",
            description = "Creates a new category for the authenticated user. Category name must be unique within the user's scope."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Category created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "message": "Category created successfully.",
                                      "data": {
                                        "id": 1,
                                        "name": "Food",
                                        "categoryKey": "FOOD",
                                        "userId": 42,
                                        "isSystemCategory": false,
                                        "createdAt": "2026-04-21T10:30:00",
                                        "updatedAt": "2026-04-21T10:30:00"
                                      },
                                      "timestamp": "2026-04-21T10:30:00"
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validation failed",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "message": "Category name is required.",
                                      "errorCode": "ET-4001",
                                      "timestamp": "2026-04-21T10:30:00"
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Duplicate category key",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "message": "Category key already exists.",
                                      "errorCode": "ET-3004",
                                      "timestamp": "2026-04-21T10:30:00"
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized — missing or invalid JWT token",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Parameter(description = "Accept-Language header for i18n (en, hi, te)", example = "en")
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String acceptLanguage,
            @Valid @RequestBody CategoryRequest request) {

        AppLogger.info(log, "category.controller.create", "name", request.getName(), "key", request.getCategoryKey());
        CategoryResponse response = categoryService.createCategory(request);

        String message = messageSource.getMessage(
                "success.category.created",
                null,
                LocaleContextHolder.getLocale()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.<CategoryResponse>builder()
                        .success(true)
                        .message(message)
                        .data(response)
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    // ── GET /api/v1/categories ────────────────────────────────────────────────

    /**
     * Retrieves all categories accessible by the authenticated user (system + user-owned), paginated.
     * Supports sorting and direction (ASC/DESC only).
     *
     * Sample request: /api/v1/categories?page=0&size=20&sort=id&direction=DESC
     *
     * HTTP 200 OK on success.
     */
    @GetMapping
    @Operation(
            summary = "List all categories",
            description = "Retrieves all categories accessible by the authenticated user (system + user-owned), paginated. Supports sort and direction (ASC/DESC only)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Categories retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PaginatedResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "data": [
                                        {
                                          "id": 1,
                                          "name": "Food",
                                          "categoryKey": "FOOD",
                                          "userId": null,
                                          "isSystemCategory": true,
                                          "createdAt": "2026-04-21T10:00:00",
                                          "updatedAt": "2026-04-21T10:00:00"
                                        },
                                        {
                                          "id": 14,
                                          "name": "Fitness",
                                          "categoryKey": "FITNESS",
                                          "userId": 42,
                                          "isSystemCategory": false,
                                          "createdAt": "2026-04-21T10:30:00",
                                          "updatedAt": "2026-04-21T10:30:00"
                                        }
                                      ],
                                      "pagination": {
                                        "current_page": 1,
                                        "page_size": 20,
                                        "total_items": 15,
                                        "total_pages": 1,
                                        "has_next": false,
                                        "links": {
                                          "next": null,
                                          "prev": null
                                        }
                                      }
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validation failed (invalid sort direction)",
                    content = @Content(mediaType = "application/json")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized — missing or invalid JWT token",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<ApiResponse<PaginatedResponse<CategoryResponse>>> getAllCategories(
            @Parameter(description = "Accept-Language header for i18n (en, hi, te)", example = "en")
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String acceptLanguage,
            @ParameterObject @Valid PageableRequest pageableRequest) {

        if (!com.expensetracker.enums.SortDirection.isValid(pageableRequest.getDirection())) {
            String msg = messageSource.getMessage("error.validation.invalidFormat", null, LocaleContextHolder.getLocale());
            return ResponseEntity.badRequest().body(ApiResponse.<PaginatedResponse<CategoryResponse>>builder()
                    .success(false)
                    .message(msg)
                    .errorCode("ET-4004")
                    .timestamp(java.time.LocalDateTime.now())
                    .build());
        }

        @SuppressWarnings("null")
        org.springframework.data.domain.Sort.Direction sortDir = org.springframework.data.domain.Sort.Direction.fromString(pageableRequest.getDirection());
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
                pageableRequest.getPage(),
                pageableRequest.getSize(),
                org.springframework.data.domain.Sort.by(sortDir, pageableRequest.getSort())
        );

        AppLogger.info(log, "category.controller.list", "page", pageable.getPageNumber(), "size", pageable.getPageSize(), "sort", pageableRequest.getSort(), "direction", pageableRequest.getDirection());
        PaginatedResponse<CategoryResponse> categories = categoryService.getAllCategories(pageable);

        String message = messageSource.getMessage(
                "success.category.list",
                null,
                LocaleContextHolder.getLocale()
        );

        ApiResponse<PaginatedResponse<CategoryResponse>> response = ApiResponse.<PaginatedResponse<CategoryResponse>>builder()
                .success(true)
                .message(message)
                .data(categories)
                .timestamp(java.time.LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    // ── GET /api/v1/categories/{id} ───────────────────────────────────────────

    /**
     * Retrieves a single category by ID.
     *
     * HTTP 200 OK on success.
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Get category by ID",
            description = "Retrieves a single category by ID if accessible by the authenticated user."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Category retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "message": "Category retrieved successfully.",
                                      "data": {
                                        "id": 1,
                                        "name": "Food",
                                        "categoryKey": "FOOD",
                                        "userId": 42,
                                        "isSystemCategory": false,
                                        "createdAt": "2026-04-21T10:30:00",
                                        "updatedAt": "2026-04-21T10:30:00"
                                      },
                                      "timestamp": "2026-04-21T10:30:00"
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Category not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "message": "Category not found.",
                                      "errorCode": "ET-3001",
                                      "timestamp": "2026-04-21T10:30:00"
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized — missing or invalid JWT token",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(
            @Parameter(description = "Accept-Language header for i18n (en, hi, te)", example = "en")
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String acceptLanguage,
            @Parameter(description = "Category ID", required = true, example = "1")
            @PathVariable Long id) {

        AppLogger.info(log, "category.controller.getById", "categoryId", id);
        CategoryResponse response = categoryService.getCategoryById(id);

        String message = messageSource.getMessage(
                "success.category.retrieved",
                null,
                LocaleContextHolder.getLocale()
        );

        return ResponseEntity
                .ok(ApiResponse.<CategoryResponse>builder()
                        .success(true)
                        .message(message)
                        .data(response)
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    // ── PUT /api/v1/categories/{id} ───────────────────────────────────────────

    /**
     * Updates an existing category.
     * Only user-owned categories can be updated (system categories are read-only).
     *
     * HTTP 200 OK on success.
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "Update category",
            description = "Updates an existing category. Only user-owned categories can be updated (system categories are read-only)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Category updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "message": "Category updated successfully.",
                                      "data": {
                                        "id": 1,
                                        "name": "Updated Food Category",
                                        "categoryKey": "FOOD",
                                        "userId": 42,
                                        "isSystemCategory": false,
                                        "createdAt": "2026-04-21T10:30:00",
                                        "updatedAt": "2026-04-23T11:45:00"
                                      },
                                      "timestamp": "2026-04-23T11:45:00"
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validation failed",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "message": "Category name is required.",
                                      "errorCode": "ET-4001",
                                      "timestamp": "2026-04-23T11:45:00"
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied — user cannot modify this category",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "message": "Access denied to category.",
                                      "errorCode": "ET-3003",
                                      "timestamp": "2026-04-23T11:45:00"
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Category not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "message": "Category not found.",
                                      "errorCode": "ET-3001",
                                      "timestamp": "2026-04-23T11:45:00"
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized — missing or invalid JWT token",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @Parameter(description = "Accept-Language header for i18n (en, hi, te)", example = "en")
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String acceptLanguage,
            @Parameter(description = "Category ID", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody com.expensetracker.dto.request.CategoryUpdateRequest request) {

        AppLogger.info(log, "category.controller.update", "categoryId", id, "name", request.getName());
        CategoryResponse response = categoryService.updateCategory(id, request);

        String message = messageSource.getMessage(
                "success.category.updated",
                null,
                LocaleContextHolder.getLocale()
        );

        return ResponseEntity
                .ok(ApiResponse.<CategoryResponse>builder()
                        .success(true)
                        .message(message)
                        .data(response)
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    // ── DELETE /api/v1/categories/{id} ────────────────────────────────────────

    /**
     * Soft-deletes a category.
     * Only user-owned categories can be deleted (system categories are read-only).
     *
     * HTTP 204 No Content on success.
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete category",
            description = "Soft-deletes a category. Only user-owned categories can be deleted (system categories are read-only)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "204",
                    description = "Category deleted successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied — user cannot delete this category",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "message": "Access denied to category.",
                                      "errorCode": "ET-3003",
                                      "timestamp": "2026-04-23T11:45:00"
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Category not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "message": "Category not found.",
                                      "errorCode": "ET-3001",
                                      "timestamp": "2026-04-23T11:45:00"
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized — missing or invalid JWT token",
                    content = @Content(mediaType = "application/json")
            )
    })
        public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @Parameter(description = "Accept-Language header for i18n (en, hi, te)", example = "en")
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String acceptLanguage,
            @Parameter(description = "Category ID", required = true, example = "1")
            @PathVariable Long id) {

        AppLogger.info(log, "category.controller.delete", "categoryId", id);
        categoryService.deleteCategory(id);

        String message = messageSource.getMessage(
                "success.category.deleted",
                null,
                LocaleContextHolder.getLocale()
        );

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message(message)
                .timestamp(java.time.LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }
}
