package com.expensetracker.controller;

import com.expensetracker.dto.request.ExpenseRequest;
import com.expensetracker.dto.response.ApiResponse;
import com.expensetracker.dto.response.ExpenseMinimalResponse;
import com.expensetracker.dto.response.PaginatedResponse;
import com.expensetracker.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
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

@Slf4j
@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
@Tag(name = "Expenses", description = "Expense management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ExpenseController {
    private final ExpenseService expenseService;
    private final MessageSource messageSource;

    @PostMapping
    @Operation(summary = "Create a new expense", description = "Creates a new expense with category mappings for the authenticated user.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Expense created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed", content = @Content(mediaType = "application/json")),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json")),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "One or more categories not found", content = @Content(mediaType = "application/json"))
    })
        public ResponseEntity<ApiResponse<ExpenseMinimalResponse>> createExpense(
            @Parameter(description = "Accept-Language header", example = "en")
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String acceptLanguage,
            @Valid @RequestBody ExpenseRequest request) {
        AppLogger.info(log, "expense.controller.create", "amount", request.getAmount(), "date", request.getExpenseDate());
        ExpenseMinimalResponse response = expenseService.createExpense(request);
        String message = messageSource.getMessage(
                "success.expense.created", null, LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<ExpenseMinimalResponse>builder()
                        .success(true)
                        .message(message)
                        .data(response)
                        .timestamp(LocalDateTime.now())
                        .build());
    }


    @PutMapping("/{id}")
    @Operation(summary = "Update an expense", description = "Updates an existing expense. Only the owner can update it.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Expense updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed", content = @Content(mediaType = "application/json")),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json")),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied", content = @Content(mediaType = "application/json")),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Expense not found", content = @Content(mediaType = "application/json"))
    })
        public ResponseEntity<ApiResponse<ExpenseMinimalResponse>> updateExpense(
            @Parameter(description = "Accept-Language header", example = "en")
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String acceptLanguage,
            @Parameter(description = "Expense ID", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ExpenseRequest request) {
        AppLogger.info(log, "expense.controller.update", "expenseId", id, "amount", request.getAmount());
        ExpenseMinimalResponse response = expenseService.updateExpense(id, request);
        String message = messageSource.getMessage(
                "success.expense.updated", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.<ExpenseMinimalResponse>builder()
                .success(true)
                .message(message)
                .data(response)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an expense", description = "Soft-deletes an expense and its category mappings. Only the owner can delete it.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Expense deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json")),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied", content = @Content(mediaType = "application/json")),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Expense not found", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<ApiResponse<Void>> deleteExpense(
            @Parameter(description = "Accept-Language header", example = "en")
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String acceptLanguage,
            @Parameter(description = "Expense ID", required = true, example = "1")
            @PathVariable Long id) {
        AppLogger.info(log, "expense.controller.delete", "expenseId", id);
        expenseService.deleteExpense(id);
        String message = messageSource.getMessage(
                "success.expense.deleted", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an expense by ID", description = "Retrieves a single expense with its categories. Only the owner can access it.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Expense retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json")),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied", content = @Content(mediaType = "application/json")),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Expense not found", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<ApiResponse<ExpenseMinimalResponse>> getExpenseById(
            @Parameter(description = "Accept-Language header", example = "en")
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String acceptLanguage,
            @Parameter(description = "Expense ID", required = true, example = "1")
            @PathVariable Long id) {
        AppLogger.info(log, "expense.controller.getById", "expenseId", id);
        ExpenseMinimalResponse response = expenseService.getExpenseById(id);
        String message = messageSource.getMessage(
                "success.expense.retrieved", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.<ExpenseMinimalResponse>builder()
                .success(true)
                .message(message)
                .data(response)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping
    @Operation(
        summary = "List all expenses (paginated)",
        description = "Returns all expenses for the authenticated user, paginated. Supports sort and direction (ASC/DESC only)."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Expenses retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed (invalid sort direction)", content = @Content(mediaType = "application/json")),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<ApiResponse<PaginatedResponse<ExpenseMinimalResponse>>> getAllExpenses(
            @Parameter(description = "Accept-Language header", example = "en")
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String acceptLanguage,
            @ParameterObject @Valid PageableRequest pageableRequest) {

        if (!com.expensetracker.enums.SortDirection.isValid(pageableRequest.getDirection())) {
            String msg = messageSource.getMessage("error.validation.invalidFormat", null, LocaleContextHolder.getLocale());
            return ResponseEntity.badRequest().body(ApiResponse.<PaginatedResponse<ExpenseMinimalResponse>>builder()
                    .success(false)
                    .message(msg)
                    .errorCode("ET-4004")
                    .timestamp(LocalDateTime.now())
                    .build());
        }

        @SuppressWarnings("null")
        org.springframework.data.domain.Sort.Direction sortDir = org.springframework.data.domain.Sort.Direction.fromString(pageableRequest.getDirection());
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
                pageableRequest.getPage(),
                pageableRequest.getSize(),
                org.springframework.data.domain.Sort.by(sortDir, pageableRequest.getSort())
        );

        AppLogger.info(log, "expense.controller.list", "page", pageable.getPageNumber(), "size", pageable.getPageSize(), "sort", pageableRequest.getSort(), "direction", pageableRequest.getDirection());
        PaginatedResponse<ExpenseMinimalResponse> response = expenseService.getAllExpenses(pageable);
        String message = messageSource.getMessage(
                "success.expense.list", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.<PaginatedResponse<ExpenseMinimalResponse>>builder()
                .success(true)
                .message(message)
                .data(response)
                .timestamp(LocalDateTime.now())
                .build());
    }
}
