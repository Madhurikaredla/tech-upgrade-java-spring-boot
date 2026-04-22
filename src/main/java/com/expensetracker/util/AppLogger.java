package com.expensetracker.util;

import org.slf4j.Logger;
import org.slf4j.MDC;

/**
 * AppLogger — centralized structured logging utility for all service classes.
 *
 * Enforces CLAUDE.md §14 logging standards:
 *   - Structured key=value pair format in every log line
 *   - Consistent log levels: INFO (normal flow), WARN (access denied / not found), ERROR (failure)
 *   - Correlation ID automatically included from MDC (set by JwtAuthenticationFilter)
 *   - NEVER log passwords, tokens, or raw sensitive data
 *
 * Usage (in services):
 *   AppLogger.info(log, "expense.create", "userId", userId, "amount", amount);
 *   AppLogger.warn(log, "expense.accessDenied", "expenseId", id, "userId", userId);
 *   AppLogger.error(log, "expense.createFailed", ex, "userId", userId);
 *
 * Output format:
 *   event=expense.create userId=42 amount=100.00 correlationId=abc-123
 */
public final class AppLogger {

    /** Private constructor — static utility class; not instantiable. */
    private AppLogger() {
    }

    // ── INFO ──────────────────────────────────────────────────────────────────

    /**
     * Logs a structured INFO message.
     *
     * @param logger      the SLF4J logger of the calling class (from @Slf4j)
     * @param event       the event name (e.g., "expense.create", "category.list")
     * @param keyValues   alternating key/value pairs (e.g., "userId", 42, "amount", 100.00)
     */
    public static void info(Logger logger, String event, Object... keyValues) {
        if (logger.isInfoEnabled()) {
            logger.info(buildMessage(event, keyValues));
        }
    }

    // ── WARN ──────────────────────────────────────────────────────────────────

    /**
     * Logs a structured WARN message. Use for access denied, not found, or degraded states.
     *
     * @param logger    the SLF4J logger of the calling class
     * @param event     the event name (e.g., "expense.accessDenied", "category.notFound")
     * @param keyValues alternating key/value pairs
     */
    public static void warn(Logger logger, String event, Object... keyValues) {
        if (logger.isWarnEnabled()) {
            logger.warn(buildMessage(event, keyValues));
        }
    }

    // ── ERROR ─────────────────────────────────────────────────────────────────

    /**
     * Logs a structured ERROR message with the exception cause.
     * NEVER log passwords, tokens, or raw sensitive request/response bodies here.
     *
     * @param logger    the SLF4J logger of the calling class
     * @param event     the event name (e.g., "expense.createFailed", "db.error")
     * @param throwable the exception that caused the error
     * @param keyValues alternating key/value pairs
     */
    public static void error(Logger logger, String event, Throwable throwable, Object... keyValues) {
        if (logger.isErrorEnabled()) {
            logger.error(buildMessage(event, keyValues), throwable);
        }
    }

    /**
     * Logs a structured ERROR message without an exception.
     *
     * @param logger    the SLF4J logger of the calling class
     * @param event     the event name
     * @param keyValues alternating key/value pairs
     */
    public static void error(Logger logger, String event, Object... keyValues) {
        if (logger.isErrorEnabled()) {
            logger.error(buildMessage(event, keyValues));
        }
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    /**
     * Builds a structured log message in key=value format.
     * Automatically appends the correlationId from MDC if present.
     *
     * Format: "event={event} key1=value1 key2=value2 [correlationId={id}]"
     *
     * @param event     the event name
     * @param keyValues alternating key/value pairs (must be even length)
     * @return the formatted log message string
     */
    private static String buildMessage(String event, Object[] keyValues) {
        StringBuilder sb = new StringBuilder();
        sb.append("event=").append(event);

        if (keyValues != null) {
            for (int i = 0; i + 1 < keyValues.length; i += 2) {
                sb.append(' ')
                  .append(keyValues[i])
                  .append('=')
                  .append(keyValues[i + 1]);
            }
        }

        // Append correlationId from MDC if set by JwtAuthenticationFilter
        String correlationId = MDC.get("correlationId");
        if (correlationId != null) {
            sb.append(" correlationId=").append(correlationId);
        }

        return sb.toString();
    }
}
