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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 * CustomAccessDeniedHandler — handles 403 Forbidden errors.
 *
 * Triggered when an authenticated user tries to access a resource they don't have permission for.
 * Returns a structured JSON error response following the ApiResponse standard.
 *
 * Error Code: ET-1007 (FORBIDDEN)
 * HTTP Status: 403
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final MessageSource messageSource;
    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage(
                ErrorCode.FORBIDDEN.getMessageKey(),
                null,
                "Access denied.",
                locale
        );

        AppLogger.warn(log, "auth.accessDenied", "uri", request.getRequestURI(),
                "user", request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "anonymous");

        ApiResponse<Void> errorResponse = ApiResponse.<Void>builder()
                .success(false)
                .message(message)
                .errorCode(ErrorCode.FORBIDDEN.getCode())
                .timestamp(LocalDateTime.now())
                .build();

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
