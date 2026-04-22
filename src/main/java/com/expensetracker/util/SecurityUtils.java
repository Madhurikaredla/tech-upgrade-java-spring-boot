package com.expensetracker.util;

import com.expensetracker.exception.AppException;
import com.expensetracker.exception.ErrorCode;
import com.expensetracker.util.AppLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * SecurityUtils — utility for extracting the authenticated user's identity from
 * the Spring Security context.
 *
 * Security rule (CLAUDE.md §9): userId must ALWAYS be extracted from the JWT via
 * the security context — it must NEVER be trusted from the request body.
 *
 * This class is the single authoritative source of the current principal's email.
 * The service layer uses the email to look up the User entity and obtain the userId.
 */
@Slf4j
@Component
public class SecurityUtils {

    /**
     * Returns the email (Spring Security username) of the currently authenticated user.
     *
     * The email is extracted from the Authentication object set by
     * {@link com.expensetracker.security.JwtAuthenticationFilter} after
     * validating the JWT token on the current request.
     *
     * @return the authenticated user's email address
     * @throws AppException UNAUTHORIZED if there is no authenticated principal in the context
     */
    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            AppLogger.warn(log, "security.noPrincipal");
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }

        if (principal instanceof String email) {
            return email;
        }

        AppLogger.warn(log, "security.unexpectedPrincipalType", "type", principal.getClass().getName());
        throw new AppException(ErrorCode.UNAUTHORIZED);
    }
}
