package com.expensetracker.security;

import com.expensetracker.exception.AppException;
import com.expensetracker.util.AppLogger;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * JwtAuthenticationFilter — intercepts every HTTP request to:
 *
 *   1. Assign a unique correlation ID to MDC so every log line for this
 *      request is traceable (CLAUDE.md §14 — correlation ID requirement).
 *   2. Extract the Bearer token from the Authorization header.
 *   3. Validate the token via JwtTokenProvider.
 *   4. Load the UserDetails and set the authentication in SecurityContext.
 *
 * If no token is present the request passes through unauthenticated —
 * Spring Security will reject it at the endpoint level if auth is required.
 *
 * Token validation errors are caught and logged; the request continues
 * without authentication so Spring Security returns 401 at the endpoint.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** MDC key used for the per-request correlation ID. */
    private static final String MDC_CORRELATION_ID_KEY = "correlationId";

    /** Authorization header name. */
    private static final String AUTHORIZATION_HEADER = "Authorization";

    /** Bearer token prefix. */
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // ── 1. Set correlation ID in MDC for structured logging ───────────
        String correlationId = UUID.randomUUID().toString();
        MDC.put(MDC_CORRELATION_ID_KEY, correlationId);
        // Echo the correlation ID back to the caller for client-side tracing
        response.setHeader("X-Correlation-Id", correlationId);

        try {
            // ── 2. Extract Bearer token from Authorization header ─────────
            String token = extractBearerToken(request);

            // ── 3. Validate token and populate SecurityContext ────────────
            if (StringUtils.hasText(token)
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                authenticateFromToken(token, request);
            }

            filterChain.doFilter(request, response);

        } finally {
            // Always clear MDC to prevent context leakage across thread reuse
            MDC.remove(MDC_CORRELATION_ID_KEY);
        }
    }

    /**
     * Loads the user identified by the token's subject and sets the authentication
     * in the SecurityContext if the token is valid.
     *
     * @param token   the raw JWT string (without "Bearer " prefix)
     * @param request the current HTTP request (for WebAuthenticationDetails)
     */
    private void authenticateFromToken(String token, HttpServletRequest request) {
        try {
            String username = jwtTokenProvider.extractUsername(token);

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtTokenProvider.isTokenValid(token, userDetails)) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities());

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                AppLogger.info(log, "jwt.authSet", "user", username, "correlationId", MDC.get(MDC_CORRELATION_ID_KEY));
            }
        } catch (AppException exception) {
            // TOKEN_EXPIRED or TOKEN_INVALID — store on request so the entry point
            // can return the exact error code/message instead of a generic 401.
            AppLogger.warn(log, "jwt.authFailed", "errorCode", exception.getErrorCode().getCode(),
                    "correlationId", MDC.get(MDC_CORRELATION_ID_KEY));
            request.setAttribute("app.exception", exception);
        }
    }

    /**
     * Extracts the raw JWT from the Authorization header.
     * Returns null if the header is absent or does not start with "Bearer ".
     *
     * @param request the HTTP servlet request
     * @return raw JWT string or null
     */
    private String extractBearerToken(HttpServletRequest request) {
        String headerValue = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(headerValue) && headerValue.startsWith(BEARER_PREFIX)) {
            return headerValue.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
