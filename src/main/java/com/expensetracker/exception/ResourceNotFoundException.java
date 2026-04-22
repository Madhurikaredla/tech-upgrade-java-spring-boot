package com.expensetracker.exception;

/**
 * ResourceNotFoundException — thrown when a requested resource does not exist
 * or has been soft-deleted.
 *
 * Maps to HTTP 404 Not Found via the {@link ErrorCode} carried on the parent
 * {@link AppException}.  GlobalExceptionHandler handles this automatically.
 *
 * Usage example:
 *   throw new ResourceNotFoundException(ErrorCode.EXPENSE_NOT_FOUND);
 */
public class ResourceNotFoundException extends AppException {

    /**
     * Constructs a ResourceNotFoundException with a typed {@link ErrorCode}.
     * The code must be one of the 404-class entries (ET-1001, ET-2001, ET-3001, …).
     *
     * @param errorCode the typed error code identifying what was not found
     */
    public ResourceNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * Constructs a ResourceNotFoundException wrapping an underlying cause.
     * Always include the original cause — never swallow it silently.
     *
     * @param errorCode the typed error code identifying what was not found
     * @param cause     the original exception that triggered this one
     */
    public ResourceNotFoundException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
