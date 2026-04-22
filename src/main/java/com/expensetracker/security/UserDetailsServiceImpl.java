package com.expensetracker.security;

import com.expensetracker.entity.User;
import com.expensetracker.exception.AppException;
import com.expensetracker.exception.ErrorCode;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.util.AppLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * UserDetailsServiceImpl — loads user-specific data for Spring Security authentication.
 *
 * Spring Security calls {@code loadUserByUsername} during authentication to obtain
 * the UserDetails object that is compared against the provided credentials.
 *
 * The method throws a typed {@link AppException} with {@link ErrorCode#USER_NOT_FOUND}
 * on lookup failure instead of the generic Spring {@link UsernameNotFoundException},
 * allowing GlobalExceptionHandler to return a structured i18n-resolved error response.
 *
 * Query rule: only non-deleted users (is_deleted = false) are considered active.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads a user by their email address (used as the Spring Security username).
     * Only active (non-deleted) users are returned.
     *
     * @param email the user's email address
     * @return populated {@link UserDetails} for the matching active user
     * @throws UsernameNotFoundException (wrapping AppException) if no active user exists
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AppLogger.info(log, "auth.loadUser", "email", email);

        User user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> {
                    AppLogger.warn(log, "auth.userNotFound", "email", email);
                    return new AppException(ErrorCode.USER_NOT_FOUND);
                });

        // Wrap the User entity in a Spring Security UserDetails adapter.
        // All users are granted the ROLE_USER authority.
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}
