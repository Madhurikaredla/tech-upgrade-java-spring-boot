package com.expensetracker.security;

import com.expensetracker.exception.AppException;
import com.expensetracker.exception.ErrorCode;
import com.expensetracker.util.AppLogger;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JwtTokenProvider — responsible for generating, signing, and validating JWT tokens.
 *
 * Security rules enforced:
 *  - JWT secret is read ONLY from the environment variable JWT_SECRET.
 *    It must be at least 256 bits (32 characters).  It is NEVER logged.
 *  - Access token expiry  : 15 minutes (configured via app.jwt.access-token-expiry-ms)
 *  - Refresh token expiry : 7 days     (configured via app.jwt.refresh-token-expiry-ms)
 *  - Claims extraction validates the signature — any tampering throws TOKEN_INVALID.
 *  - Expired tokens throw TOKEN_EXPIRED so the client can request a refresh.
 */
@Slf4j
@Component
public class JwtTokenProvider {

    /** HMAC-SHA signing key derived from the JWT_SECRET environment variable. */
    private final SecretKey signingKey;

    /** Access token validity in milliseconds (default: 15 minutes). */
    private final long accessTokenExpiryMs;

    /** Refresh token validity in milliseconds (default: 7 days). */
    private final long refreshTokenExpiryMs;

    /**
     * Constructor injection.
     * Spring reads JWT_SECRET from the environment — NEVER from yml/properties.
     *
     * @param jwtSecret            raw secret string from env var JWT_SECRET
     * @param accessTokenExpiryMs  access token expiry millis from application.yml
     * @param refreshTokenExpiryMs refresh token expiry millis from application.yml
     */
    public JwtTokenProvider(
            @Value("${JWT_SECRET}") String jwtSecret,
            @Value("${app.jwt.access-token-expiry-ms}") long accessTokenExpiryMs,
            @Value("${app.jwt.refresh-token-expiry-ms}") long refreshTokenExpiryMs) {

        // Derive a cryptographically secure HMAC-SHA key from the provided secret.
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiryMs = accessTokenExpiryMs;
        this.refreshTokenExpiryMs = refreshTokenExpiryMs;
    }

    // ── Token Generation ─────────────────────────────────────────────────────

    /**
     * Generates a signed JWT access token for the given user details.
     * The token subject is set to the user's email (username).
     *
     * @param userDetails the authenticated user's details
     * @return signed JWT access token string
     */
    public String generateAccessToken(UserDetails userDetails) {
        return buildToken(userDetails.getUsername(), accessTokenExpiryMs);
    }

    /**
     * Generates a signed JWT refresh token for the given user details.
     *
     * @param userDetails the authenticated user's details
     * @return signed JWT refresh token string
     */
    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(userDetails.getUsername(), refreshTokenExpiryMs);
    }

    /**
     * Builds and signs a JWT token with the given subject and expiry.
     *
     * @param subject   the principal identifier (user email)
     * @param expiryMs  token lifetime in milliseconds
     * @return compact signed JWT string
     */
    private String buildToken(String subject, long expiryMs) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiryMs);

        return Jwts.builder()
                .subject(subject)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    // ── Claims Extraction ────────────────────────────────────────────────────

    /**
     * Extracts the subject (user email) from a validated JWT token.
     *
     * @param token the compact JWT string
     * @return subject claim (user email)
     * @throws AppException TOKEN_EXPIRED if the token has passed its expiry date
     * @throws AppException TOKEN_INVALID if the token signature or structure is invalid
     */
    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * Parses and validates the JWT, returning its claims.
     * Throws typed AppException on failure so callers never see raw JwtException.
     *
     * @param token the compact JWT string
     * @return validated Claims object
     */
    private Claims extractClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException exception) {
            AppLogger.warn(log, "jwt.tokenExpired");
            throw new AppException(ErrorCode.TOKEN_EXPIRED, exception);
        } catch (JwtException | IllegalArgumentException exception) {
            AppLogger.warn(log, "jwt.tokenInvalid");
            throw new AppException(ErrorCode.TOKEN_INVALID, exception);
        }
    }

    // ── Validation ───────────────────────────────────────────────────────────

    /**
     * Validates that the token belongs to the given user and has not expired.
     *
     * @param token       the compact JWT string
     * @param userDetails the user whose identity is being verified
     * @return true if the token is valid for the supplied user; false otherwise
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String usernameFromToken = extractUsername(token);
            return usernameFromToken.equals(userDetails.getUsername());
        } catch (AppException exception) {
            // TOKEN_EXPIRED or TOKEN_INVALID — propagate so the filter handles it
            throw exception;
        }
    }
}
