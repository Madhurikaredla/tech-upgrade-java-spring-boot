package com.expensetracker.service;

import com.expensetracker.dto.request.LoginRequest;
import com.expensetracker.dto.request.RegisterRequest;
import com.expensetracker.dto.response.AuthResponse;
import com.expensetracker.entity.User;
import com.expensetracker.exception.AppException;
import com.expensetracker.exception.ErrorCode;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.security.JwtTokenProvider;
import com.expensetracker.util.AppLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AuthService — owns all authentication and registration business logic.
 *
 * Responsibilities:
 *  - register : validates email uniqueness, hashes password (BCrypt strength 12),
 *               persists the new User, and returns JWT tokens.
 *  - login    : delegates credential verification to Spring's AuthenticationManager,
 *               then issues JWT tokens on success.
 *
 * Security rules enforced here:
 *  - userId is NEVER trusted from the request body — not applicable to auth endpoints,
 *    but no userId is accepted from callers.
 *  - Raw passwords are NEVER logged.
 *  - Entities are NEVER returned to the controller — always mapped to AuthResponse.
 *  - AppException with typed ErrorCode is thrown instead of raw RuntimeException.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    // ── Register ─────────────────────────────────────────────────────────────

    /**
     * Registers a new user account.
     *
     * Steps:
     *  1. Check email uniqueness (application-level guard before the DB constraint).
     *  2. Hash the raw password with BCrypt (strength 12).
     *  3. Persist the new User entity.
     *  4. Issue access and refresh JWT tokens.
     *  5. Return AuthResponse DTO — entity is NEVER returned directly.
     *
     * @param request validated registration details
     * @return AuthResponse containing tokens and basic user info
     * @throws AppException EMAIL_ALREADY_EXISTS if the email is taken
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        AppLogger.info(log, "auth.register");  // Email masked — avoid logging PII in full

        // ── Step 1: Enforce unique email at application level ─────────────
        if (userRepository.existsByEmailAndIsDeletedFalse(request.getEmail())) {
            AppLogger.warn(log, "auth.register.emailAlreadyExists");
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // ── Step 2: Hash the password (BCrypt strength 12 configured in SecurityConfig) ─
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // ── Step 3: Persist the new user ──────────────────────────────────
        User newUser = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(hashedPassword)
                .build();

        User savedUser = userRepository.save(newUser);
        AppLogger.info(log, "auth.register.success", "userId", savedUser.getId());

        // ── Step 4: Issue JWT tokens ───────────────────────────────────────
        UserDetails userDetails = buildUserDetails(savedUser);
        String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        // ── Step 5: Map entity → DTO (entity NEVER returned to controller) ─
        return buildAuthResponse(savedUser, accessToken, refreshToken);
    }

    // ── Login ────────────────────────────────────────────────────────────────

    /**
     * Authenticates an existing user.
     *
     * Steps:
     *  1. Delegate credential verification to Spring's AuthenticationManager.
     *     This internally calls UserDetailsServiceImpl and BCrypt comparison.
     *  2. On success, issue access and refresh JWT tokens.
     *  3. Return AuthResponse DTO.
     *
     * @param request validated login credentials
     * @return AuthResponse containing tokens and basic user info
     * @throws AppException INVALID_CREDENTIALS if email or password is wrong
     * @throws AppException USER_NOT_FOUND if the email does not exist
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        AppLogger.info(log, "auth.login");   // Email masked — avoid logging PII

        // ── Step 1: Verify credentials via Spring AuthenticationManager ───
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException exception) {
            AppLogger.warn(log, "auth.login.invalidCredentials");
            throw new AppException(ErrorCode.INVALID_CREDENTIALS, exception);
        } catch (InternalAuthenticationServiceException exception) {
            // Spring wraps exceptions from UserDetailsService in InternalAuthenticationServiceException.
            // If the cause is already a typed AppException (e.g. USER_NOT_FOUND), rethrow it directly
            // so GlobalExceptionHandler returns the correct error code and message.
            if (exception.getCause() instanceof AppException appException) {
                throw appException;
            }
            AppLogger.warn(log, "auth.login.internalAuthError");
            throw new AppException(ErrorCode.INVALID_CREDENTIALS, exception);
        }

        // ── Step 2: Load full user entity to build the response ──────────
        User user = userRepository.findByEmailAndIsDeletedFalse(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // ── Step 3: Issue tokens ──────────────────────────────────────────
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        AppLogger.info(log, "auth.login.success", "userId", user.getId());

        // ── Step 4: Map entity → DTO ──────────────────────────────────────
        return buildAuthResponse(user, accessToken, refreshToken);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Builds a Spring Security UserDetails from a User entity.
     * Used after registration to generate tokens without a separate DB lookup.
     *
     * @param user the persisted User entity
     * @return Spring Security UserDetails adapter
     */
    private UserDetails buildUserDetails(User user) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles("USER")
                .build();
    }

    /**
     * Maps a User entity plus tokens to an AuthResponse DTO.
     * The entity's password field is intentionally excluded from the DTO.
     *
     * @param user         the persisted User entity
     * @param accessToken  the issued JWT access token
     * @param refreshToken the issued JWT refresh token
     * @return AuthResponse DTO
     */
    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
