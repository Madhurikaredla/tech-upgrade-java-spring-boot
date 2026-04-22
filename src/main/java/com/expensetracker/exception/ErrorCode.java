package com.expensetracker.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * ErrorCode — enumeration of all application-level error codes.
 *
 * Ranges:
 *   ET-1001 to ET-1099 : Authentication / Authorization
 *   ET-2001 to ET-2099 : Expense domain
 *   ET-3001 to ET-3099 : Category domain
 *   ET-4001 to ET-4099 : Validation
 *   ET-5001 to ET-5099 : Database
 *   ET-9001 to ET-9099 : System / Infrastructure
 *
 * Each entry carries the HTTP status that should be returned to the client
 * and the i18n message key resolved by GlobalExceptionHandler.
 */
@Getter
public enum ErrorCode {

    // ── Auth (ET-1001 – ET-1099) ──────────────────────────────────────────────
    USER_NOT_FOUND("ET-1001", HttpStatus.NOT_FOUND, "error.auth.userNotFound"),
    INVALID_CREDENTIALS("ET-1002", HttpStatus.UNAUTHORIZED, "error.auth.invalidCredentials"),
    TOKEN_EXPIRED("ET-1003", HttpStatus.UNAUTHORIZED, "error.auth.tokenExpired"),
    TOKEN_INVALID("ET-1004", HttpStatus.UNAUTHORIZED, "error.auth.tokenInvalid"),
    EMAIL_ALREADY_EXISTS("ET-1005", HttpStatus.CONFLICT, "error.auth.emailAlreadyExists"),
    UNAUTHORIZED("ET-1006", HttpStatus.UNAUTHORIZED, "error.auth.unauthorized"),
    FORBIDDEN("ET-1007", HttpStatus.FORBIDDEN, "error.auth.forbidden"),

    // ── Expense (ET-2001 – ET-2099) ───────────────────────────────────────────
    EXPENSE_NOT_FOUND("ET-2001", HttpStatus.NOT_FOUND, "error.expense.notFound"),
    INVALID_EXPENSE_AMOUNT("ET-2002", HttpStatus.BAD_REQUEST, "error.expense.invalidAmount"),
    EXPENSE_DATE_INVALID("ET-2003", HttpStatus.BAD_REQUEST, "error.expense.dateInvalid"),
    EXPENSE_CATEGORY_REQUIRED("ET-2004", HttpStatus.BAD_REQUEST, "error.expense.categoryRequired"),
    EXPENSE_ACCESS_DENIED("ET-2005", HttpStatus.FORBIDDEN, "error.expense.accessDenied"),

    // ── Category (ET-3001 – ET-3099) ──────────────────────────────────────────
    CATEGORY_NOT_FOUND("ET-3001", HttpStatus.NOT_FOUND, "error.category.notFound"),
    DUPLICATE_CATEGORY_NAME("ET-3002", HttpStatus.CONFLICT, "error.category.nameDuplicate"),
    CATEGORY_ACCESS_DENIED("ET-3003", HttpStatus.FORBIDDEN, "error.category.accessDenied"),
    DUPLICATE_CATEGORY_KEY("ET-3004", HttpStatus.CONFLICT, "error.category.keyDuplicate"),

    // ── Validation (ET-4001 – ET-4099) ────────────────────────────────────────
    VALIDATION_FAILED("ET-4001", HttpStatus.BAD_REQUEST, "error.validation.failed"),
    FIELD_REQUIRED("ET-4002", HttpStatus.BAD_REQUEST, "error.validation.fieldRequired"),
    FIELD_SIZE_EXCEEDED("ET-4003", HttpStatus.BAD_REQUEST, "error.validation.fieldSizeExceeded"),
    INVALID_FORMAT("ET-4004", HttpStatus.BAD_REQUEST, "error.validation.invalidFormat"),

    // ── Database (ET-5001 – ET-5099) ──────────────────────────────────────────
    DB_ERROR("ET-5001", HttpStatus.INTERNAL_SERVER_ERROR, "error.db.generic"),
    DB_CONSTRAINT_VIOLATION("ET-5002", HttpStatus.CONFLICT, "error.db.constraintViolation"),
    DB_DUPLICATE_ENTRY("ET-5003", HttpStatus.CONFLICT, "error.db.duplicateEntry"),
    DB_CONNECTION_FAILED("ET-5004", HttpStatus.SERVICE_UNAVAILABLE, "error.db.connectionFailed"),

    // ── System (ET-9001 – ET-9099) ────────────────────────────────────────────
    INTERNAL_SERVER_ERROR("ET-9001", HttpStatus.INTERNAL_SERVER_ERROR, "error.system.internalError"),
    SERVICE_UNAVAILABLE("ET-9002", HttpStatus.SERVICE_UNAVAILABLE, "error.system.serviceUnavailable"),
    RATE_LIMIT_EXCEEDED("ET-9003", HttpStatus.TOO_MANY_REQUESTS, "error.system.rateLimitExceeded");

    /** Unique application-level error code surfaced in every error response. */
    private final String code;

    /** HTTP status to use when this error is returned as the API response. */
    private final HttpStatus httpStatus;

    /** i18n message key resolved by GlobalExceptionHandler via MessageSource. */
    private final String messageKey;

    ErrorCode(String code, HttpStatus httpStatus, String messageKey) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.messageKey = messageKey;
    }
}
