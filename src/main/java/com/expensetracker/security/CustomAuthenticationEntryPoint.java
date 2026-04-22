package com.expensetracker.security;

import com.expensetracker.dto.response.ApiResponse;
import com.expensetracker.exception.ErrorCode;
import com.expensetracker.util.AppLogger;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 * CustomAuthenticationEntryPoint — handles 401 Unauthorized errors.
 *
 * Triggered when an unauthenticated user (no token or invalid token) tries to access a protected resource.
 * Returns a structured JSON error response following the ApiResponse standard.
 *
 * Error Code: ET-1006 (UNAUTHORIZED)
 * HTTP Status: 401
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final MessageSource messageSource;
    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        Locale locale = LocaleContextHolder.getLocale();

        // If the filter stored a typed AppException (e.g. TOKEN_EXPIRED, TOKEN_INVALID),
        // use its error code and message instead of the generic UNAUTHORIZED.
        com.expensetracker.exception.AppException appEx =
                (com.expensetracker.exception.AppException) request.getAttribute("app.exception");

        ErrorCode errorCode = appEx != null ? appEx.getErrorCode() : ErrorCode.UNAUTHORIZED;
        String message = messageSource.getMessage(
                errorCode.getMessageKey(),
                null,
                errorCode.name(),
                locale
        );

        AppLogger.warn(log, "auth.unauthorizedAccess", "uri", request.getRequestURI(),
                "errorCode", errorCode.getCode());

        ApiResponse<Void> errorResponse = ApiResponse.<Void>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode.getCode())
                .timestamp(LocalDateTime.now())
                .build();

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
