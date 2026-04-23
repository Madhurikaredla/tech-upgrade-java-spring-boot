package com.expensetracker.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LoginRequest — request body for POST /api/v1/auth/login.
 *
 * Validation rules:
 *   email    : required, valid email format, max 55 characters
 *   password : required, 8–15 characters (BCrypt input limit)
 *
 * All validation messages reference i18n property keys.
 * The password field is NEVER logged or returned in any response DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for user login")
public class LoginRequest {

    @NotBlank(message = "{error.validation.fieldRequired}")
    @Email(regexp = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]{2,}$", message = "{error.validation.invalidFormat}")
    @Size(max = 55, message = "{error.validation.fieldSizeExceeded}")
    @Schema(description = "Registered email address",
            example = "jane@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    /**
     * Raw password provided by the user.
     * BCrypt comparison happens in AuthService — the value is NEVER logged.
     */
    @NotBlank(message = "{error.validation.fieldRequired}")
    @Size(min = 8, max = 15, message = "{validation.auth.password.size}")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,15}$",
        message = "{validation.auth.password.pattern}"
    )
    @Schema(description = "Account password (8-15 chars, at least 1 uppercase, 1 lowercase, 1 digit, 1 special character)",
            example = "Secret123!", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
