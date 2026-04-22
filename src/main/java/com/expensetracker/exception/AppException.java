package com.expensetracker.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * AppException — base class for all typed application exceptions.
 *
 * Every exception thrown in the service layer must extend this class and
 * supply an {@link ErrorCode}.  Raw {@link RuntimeException} must NEVER
 * be thrown directly; use this class (or a subclass) instead.
 *
 * The {@link com.expensetracker.exception.GlobalExceptionHandler} catches
 * AppException and maps it to a structured API error response with:
 *   - success  : false
 *   - errorCode: the ET-xxxx code string
 *   - message  : i18n-resolved message from ErrorCode.messageKey
 *   - timestamp: current UTC instant
 *
 * Stack traces are NEVER exposed in API responses.
 */
@Getter
public class AppException extends RuntimeException {

    /** Typed error descriptor carrying the HTTP status and i18n key. */
    private final ErrorCode errorCode;

    /**
     * Constructs an AppException from an {@link ErrorCode}.
     * The message is the enum name — the human-readable text is resolved
     * from the i18n bundle by GlobalExceptionHandler at response time.
     *
     * @param errorCode the typed error code to associate with this exception
     */
    public AppException(ErrorCode errorCode) {
        super(errorCode.name());
        this.errorCode = errorCode;
    }

    /**
     * Constructs an AppException wrapping an underlying cause.
     * Always include the original cause so the root error is not silently swallowed.
     *
     * @param errorCode the typed error code to associate with this exception
     * @param cause     the original exception that triggered this one
     */
    public AppException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.name(), cause);
        this.errorCode = errorCode;
    }

    /**
     * Returns the HTTP status associated with this exception's error code.
     *
     * @return HTTP status (e.g. 404, 401, 500)
     */
    public HttpStatus getHttpStatus() {
        return errorCode.getHttpStatus();
    }
}
