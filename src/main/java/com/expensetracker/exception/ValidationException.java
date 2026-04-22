package com.expensetracker.exception;

/**
 * ValidationException — thrown when business-level validation fails in the
 * service layer (distinct from Bean Validation / @Valid failures which are
 * handled via MethodArgumentNotValidException).
 *
 * Maps to HTTP 400 Bad Request.
 *
 * Usage example:
 *   throw new ValidationException(ErrorCode.INVALID_EXPENSE_AMOUNT);
 */
public class ValidationException extends AppException {

    /**
     * Constructs a ValidationException with a typed {@link ErrorCode}.
     *
     * @param errorCode the typed error code describing the validation failure
     */
    public ValidationException(ErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * Constructs a ValidationException wrapping an underlying cause.
     *
     * @param errorCode the typed error code describing the validation failure
     * @param cause     the original exception that triggered this one
     */
    public ValidationException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
