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
 * RegisterRequest — request body for POST /api/v1/auth/register.
 *
 * Validation rules:
 *   name     : required, max 55 characters
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
@Schema(description = "Request body for new user registration")
public class RegisterRequest {

        @NotBlank(message = "{error.validation.fieldRequired}")
        @Size(min = 2, max = 55, message = "{error.validation.fieldSizeExceeded}")
        @Schema(description = "User's display name", example = "Jane Doe", requiredMode = Schema.RequiredMode.REQUIRED)
        private String name;

        @NotBlank(message = "{error.validation.fieldRequired}")
        @Email(regexp = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]{2,}$", message = "{error.validation.invalidFormat}")
        @Size(max = 55, message = "{error.validation.fieldSizeExceeded}")
        @Schema(description = "User's email address — used as the login identifier",
                example = "jane@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        private String email;

        /**
         * Raw password — validated here and immediately hashed by AuthService.
         * Must be 8–15 characters to satisfy BCrypt constraints.
         * NEVER logged, stored in plaintext, or returned in any response.
         */
        @NotBlank(message = "{error.validation.fieldRequired}")
        @Size(min = 8, max = 15, message = "{validation.auth.password.size}")        @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,15}$",
        message = "{validation.auth.password.pattern}"
        )
        @Schema(description = "User's password (8-15 chars, at least 1 uppercase, 1 lowercase, 1 digit, 1 special character)", example = "Secret123!", requiredMode = Schema.RequiredMode.REQUIRED)
        private String password;
}
