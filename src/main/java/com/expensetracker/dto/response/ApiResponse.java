package com.expensetracker.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ApiResponse — generic wrapper for all API responses.
 *
 * Every endpoint returns this structure with fields:
 *   success   : true for successful calls, false for errors
 *   message   : i18n-resolved human-readable description
 *   errorCode : ET-xxxx code (null on success)
 *   data      : response payload (null on error)
 *   timestamp : UTC instant when the response was generated
 *
 * Null fields are excluded from the serialized JSON (Jackson @JsonInclude).
 *
 * @param <T> type of the data payload
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard API response wrapper")
public class ApiResponse<T> {

    @Schema(description = "Indicates whether the request succeeded", example = "true")
    private boolean success;

    @Schema(description = "Human-readable message describing the outcome",
            example = "User registered successfully.")
    private String message;

    @Schema(description = "Application error code (present only on failure)", example = "ET-1005")
    private String errorCode;

    @Schema(description = "Response payload; null on error")
    private T data;

    @Builder.Default
    @Schema(description = "UTC timestamp of the response", example = "2026-04-18T12:34:56")
    private LocalDateTime timestamp = LocalDateTime.now();

    // ── Factory helpers ────────────────────────────────────────────────────

    /**
     * Creates a successful response with a payload.
     *
     * @param message i18n-resolved success message
     * @param data    the response payload
     * @param <T>     payload type
     * @return populated success ApiResponse
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Creates a successful response without a payload (e.g., DELETE 204).
     *
     * @param message i18n-resolved success message
     * @param <T>     unused type param
     * @return populated success ApiResponse with null data
     */
    public static <T> ApiResponse<T> success(String message) {
        return success(message, null);
    }
}
