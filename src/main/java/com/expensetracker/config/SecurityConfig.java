package com.expensetracker.config;

import com.expensetracker.security.CustomAccessDeniedHandler;
import com.expensetracker.security.CustomAuthenticationEntryPoint;
import com.expensetracker.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SecurityConfig — Spring Security configuration for the Expense Tracker API.
 *
 * Design decisions:
 *  - Stateless session (JWT-based) — no HttpSession is created or used.
 *  - CSRF disabled — stateless REST with Bearer tokens has no CSRF exposure.
 *  - CORS — explicit origins only (/allowedOrigins("*") is forbidden in production).
 *  - BCrypt password encoder with strength 12 as required by CLAUDE.md §16.
 *  - Only /api/v1/auth/** and Swagger UI paths are permit-all.
 *  - All other endpoints require a valid JWT.
 *  - Actuator /health endpoint is accessible without auth (readiness probes).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    /**
     * Paths that are publicly accessible without a JWT token.
     * All other paths require authentication.
     */
    private static final String[] PUBLIC_PATHS = {
            "/api/v1/auth/**",          // register and login
            "/swagger-ui/**",           // Swagger UI assets
            "/swagger-ui.html",
            "/api-docs/**",             // OpenAPI spec
            "/actuator/health"          // health check for load balancer / start.sh
    };

    /**
     * Configures the security filter chain.
     *
     * @param http the HttpSecurity builder
     * @return the configured SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // ── CSRF disabled — stateless JWT API ──────────────────────────────
                .csrf(AbstractHttpConfigurer::disable)

                // ── Authorization rules ────────────────────────────────────────────
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        // Preflight OPTIONS requests must be permitted for CORS
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Every other request requires a valid JWT
                        .anyRequest().authenticated()
                )

                // ── Stateless session — no HttpSession ────────────────────────────

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Exception handling — custom 401 and 403 error responses
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                )

                // ── Authentication provider ────────────────────────────────────────
                .authenticationProvider(authenticationProvider())

                // ── JWT filter runs before the standard username/password filter ──
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Authentication provider that uses UserDetailsService + BCrypt.
     * BCrypt is configured at strength 12 as required by CLAUDE.md §16.
     *
     * @return configured DaoAuthenticationProvider
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * BCrypt password encoder with strength 12.
     * Strength 12 balances security (slow enough to resist brute force)
     * with acceptable hash time (~300 ms on modern hardware).
     *
     * @return BCryptPasswordEncoder with strength 12
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Exposes the AuthenticationManager as a Spring bean so AuthService can
     * inject it for the login flow.
     *
     * @param config Spring's AuthenticationConfiguration
     * @return the application's AuthenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
