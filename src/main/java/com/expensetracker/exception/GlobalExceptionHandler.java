package com.expensetracker.exception;

import com.expensetracker.dto.response.ApiResponse;
import com.expensetracker.util.AppLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.Locale;

/**
 * GlobalExceptionHandler — centralized exception-to-response mapping.
 *
 * Handles:
 *   1. AppException (and all subclasses) — typed application errors
 *   2. MethodArgumentNotValidException   — @Valid bean validation failures
 *   3. DataAccessException               — Spring Data / JDBC failures mapped to ET-5001
 *   4. Exception (catch-all)             — unexpected errors mapped to ET-9001
 *
 * Rules enforced:
 *   - Stack traces are NEVER included in API responses.
 *   - All user-facing messages are resolved through MessageSource (i18n).
 *   - Every error response includes: success=false, errorCode, message, timestamp.
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    // ── NoResourceFoundException (404 for static resources, e.g. /swagger-ui, /favicon.ico) ──

    /**
     * Handles Spring MVC NoResourceFoundException (missing static resource or unmapped route).
     * Logged at DEBUG to avoid polluting logs with browser/tool requests for favicon, etc.
     * Returns 404 without an errorCode since this is not an application-level error.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFoundException(
            NoResourceFoundException exception) {
        log.debug("No resource found path={}", exception.getResourcePath());
        return buildErrorResponse(
                ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                "Resource not found: " + exception.getResourcePath(),
                HttpStatus.NOT_FOUND);
    }

    // ── AppException (covers ResourceNotFoundException, ValidationException, etc.) ──

    /**
     * Handles all typed AppException instances thrown by the service layer.
     * The error code's i18n message key is resolved using the request locale.
     * For 4xx client errors: log at WARN without stack trace.
     * For 5xx server errors: log at ERROR with stack trace.
     */
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException exception) {
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage(
                exception.getErrorCode().getMessageKey(),
                null,
                exception.getErrorCode().name(),   // fallback if key not found
                locale
        );

        // Log 5xx errors with stack trace; 4xx client errors only at WARN without trace
        if (exception.getHttpStatus().is5xxServerError()) {
            AppLogger.error(log, "exception.appError", exception,
                    "errorCode", exception.getErrorCode().getCode(), "message", message);
        } else {
            AppLogger.warn(log, "exception.appError",
                    "errorCode", exception.getErrorCode().getCode(), "message", message);
        }

        return buildErrorResponse(exception.getErrorCode().getCode(), message,
                exception.getHttpStatus());
    }

    // ── Bean-validation failures (@Valid) ─────────────────────────────────────

    /**
     * Handles constraint violations raised by @Valid on controller method parameters.
     * All field errors are collected into a single comma-separated message.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException exception) {

        Locale locale = LocaleContextHolder.getLocale();

        // Collect all field-level validation errors with field names (e.g. "email: Invalid email format")
        List<String> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();

        String joinedErrors = String.join("; ", errors);

        String message = joinedErrors;

        AppLogger.warn(log, "exception.validationFailed", "errors", joinedErrors);
        return buildErrorResponse(ErrorCode.VALIDATION_FAILED.getCode(),
                message,
                HttpStatus.BAD_REQUEST);
    }

    // ── Spring Data / JDBC errors ──────────────────────────────────────────────

    /**
     * Handles DataAccessException thrown by the repository layer.
     * Always mapped to ET-5001 (generic DB error) to avoid leaking schema details.
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataAccessException(
            DataAccessException exception) {

        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage(
                ErrorCode.DB_ERROR.getMessageKey(), null,
                ErrorCode.DB_ERROR.name(), locale);

        AppLogger.error(log, "exception.dataAccess", exception, "errorCode", ErrorCode.DB_ERROR.getCode());
        return buildErrorResponse(ErrorCode.DB_ERROR.getCode(), message,
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ── Catch-all ─────────────────────────────────────────────────────────────

    /**
     * Handles any uncaught exception, mapped to ET-9001 (internal server error).
     * Stack trace is logged at ERROR level but NEVER exposed in the response.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception exception) {
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage(
                ErrorCode.INTERNAL_SERVER_ERROR.getMessageKey(), null,
                ErrorCode.INTERNAL_SERVER_ERROR.name(), locale);

        AppLogger.error(log, "exception.unhandled", exception,
                "errorCode", ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                "exceptionType", exception.getClass().getSimpleName());
        return buildErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), message,
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ── Helper ─────────────────────────────────────────────────────────────────

    /**
     * Builds a standardised error {@link ApiResponse} wrapped in a {@link ResponseEntity}.
     *
     * @param errorCode the ET-xxxx code string
     * @param message   i18n-resolved human-readable error description
     * @param status    HTTP status code to return
     * @return response entity with the error payload
     */
    private ResponseEntity<ApiResponse<Void>> buildErrorResponse(
            String errorCode, String message, HttpStatus status) {

        ApiResponse<Void> body = ApiResponse.<Void>builder()
                .success(false)
                .errorCode(errorCode)
                .message(message)
                .data(null)
                .build();
        return ResponseEntity.status(status).body(body);
    }
}
