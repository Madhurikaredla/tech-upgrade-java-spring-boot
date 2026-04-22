package com.expensetracker.controller;

import com.expensetracker.dto.request.LoginRequest;
import com.expensetracker.dto.request.RegisterRequest;
import com.expensetracker.dto.response.ApiResponse;
import com.expensetracker.dto.response.AuthResponse;
import com.expensetracker.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.expensetracker.util.AppLogger;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AuthController — handles user registration and login endpoints.
 *
 * Base path : /api/v1/auth
 * Public    : all endpoints in this controller are permit-all (no JWT required).
 *
 * Controller rules enforced:
 *  - No business logic — all processing delegated to AuthService.
 *  - No @Transactional — transaction boundary belongs in the service layer.
 *  - No direct repository access.
 *  - All responses wrapped in ApiResponse<T>.
 *  - All request DTOs validated with @Valid.
 *  - Constructor injection only.
 *  - Accept-Language header documented on all endpoints for i18n support.
 */
@Slf4j // Class-level annotations for logging, REST controller, request mapping, constructor injection, and OpenAPI tagging
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "User registration and authentication endpoints")
public class AuthController {

    private final AuthService authService;

    // ── POST /api/v1/auth/register ────────────────────────────────────────────

    /**
     * Registers a new user account and returns JWT tokens on success.
     *
     * HTTP 201 Created on success.
     */
    @PostMapping("/register")
    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account. Returns JWT access and refresh tokens."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "message": "User registered successfully.",
                                      "data": {
                                        "userId": 1,
                                        "name": "Jane Doe",
                                        "email": "jane@example.com",
                                        "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
                                        "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
                                      },
                                      "timestamp": "2026-04-20T10:00:00"
                                    }"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validation failed — missing or invalid fields",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "message": "A required field is missing.",
                                      "errorCode": "ET-4001",
                                      "timestamp": "2026-04-20T10:00:00"
                                    }"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Email address already registered",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "message": "An account with this email address already exists.",
                                      "errorCode": "ET-1005",
                                      "timestamp": "2026-04-20T10:00:00"
                                    }"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "message": "An unexpected error occurred. Please try again later.",
                                      "errorCode": "ET-9001",
                                      "timestamp": "2026-04-20T10:00:00"
                                    }""")))
    })
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            @Parameter(description = "Preferred response language (en, hi, te)", example = "en")
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage) {

        AppLogger.info(log, "auth.register", "email", request.getEmail());
        AuthResponse authResponse = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully.", authResponse));
    }

    // ── POST /api/v1/auth/login ───────────────────────────────────────────────

    /**
     * Authenticates an existing user and returns JWT tokens on success.
     *
     * HTTP 200 OK on success.
     */
    @PostMapping("/login")
    @Operation(
            summary = "Log in an existing user",
            description = "Authenticates the user with email and password. Returns JWT tokens."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "message": "Login successful.",
                                      "data": {
                                        "userId": 1,
                                        "name": "Jane Doe",
                                        "email": "jane@example.com",
                                        "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
                                        "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
                                      },
                                      "timestamp": "2026-04-20T10:00:00"
                                    }"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validation failed — missing or invalid fields",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "message": "A required field is missing.",
                                      "errorCode": "ET-4001",
                                      "timestamp": "2026-04-20T10:00:00"
                                    }"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Invalid email or password",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "message": "Invalid email or password.",
                                      "errorCode": "ET-1002",
                                      "timestamp": "2026-04-20T10:00:00"
                                    }"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "message": "User not found.",
                                      "errorCode": "ET-1001",
                                      "timestamp": "2026-04-20T10:00:00"
                                    }"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "message": "An unexpected error occurred. Please try again later.",
                                      "errorCode": "ET-9001",
                                      "timestamp": "2026-04-20T10:00:00"
                                    }""")))
    })
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            @Parameter(description = "Preferred response language (en, hi, te)", example = "en")
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage) {

        AppLogger.info(log, "auth.login", "email", request.getEmail());
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful.", authResponse));
    }
}
