package com.expensetracker.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AuthResponse — payload returned by the register and login endpoints.
 *
 * Contains:
 *   userId       : the newly registered or authenticated user's ID
 *   name         : user's display name
 *   email        : user's login email
 *   accessToken  : short-lived JWT for authenticating API requests (15 min)
 *   refreshToken : long-lived JWT for obtaining a new access token (7 days)
 *
 * Security rule: the user's password hash is NEVER included here.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Authentication response returned after successful register or login")
public class AuthResponse {

    @Schema(description = "Unique identifier of the authenticated user", example = "42")
    private Long userId;

    @Schema(description = "Display name of the authenticated user", example = "Jane Doe")
    private String name;

    @Schema(description = "Email address of the authenticated user", example = "jane@example.com")
    private String email;

    @Schema(description = "JWT access token — include as 'Authorization: Bearer <token>'",
            example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;

    @Schema(description = "JWT refresh token — use to obtain a fresh access token",
            example = "eyJhbGciOiJIUzI1NiJ9...")
    private String refreshToken;
}
