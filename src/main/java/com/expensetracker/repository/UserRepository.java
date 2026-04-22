package com.expensetracker.repository;

import com.expensetracker.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * UserRepository — Spring Data JPA repository for the {@link User} entity.
 *
 * Repository rules:
 *  - All queries filter is_deleted = false (soft-delete safety).
 *  - List queries use Pageable — no unbounded findAll().
 *  - Method names clearly describe what they query.
 *  - JPQL used for all derived queries; native SQL only if JPQL is insufficient.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds an active (non-deleted) user by their email address.
     * Used by UserDetailsServiceImpl for authentication and by AuthService
     * to detect duplicate email at registration.
     *
     * @param email the user's email address
     * @return Optional containing the User if found and not soft-deleted
     */
    Optional<User> findByEmailAndIsDeletedFalse(String email);

    /**
     * Checks whether an active user with the given email already exists.
     * Used by AuthService to enforce the unique-email constraint at
     * the application level before delegating to the DB constraint.
     *
     * @param email the email address to check
     * @return true if an active user with this email exists
     */
    boolean existsByEmailAndIsDeletedFalse(String email);

    /**
     * Returns all active (non-deleted) users with pagination.
     * Provided for admin use; never called from the auth flow directly.
     *
     * @param pageable pagination parameters
     * @return page of active users
     */
    Page<User> findAllByIsDeletedFalse(Pageable pageable);

     /**
     * For compatibility with service layer code expecting findByEmail
     */
    default Optional<User> findByEmail(String email) {
        return findByEmailAndIsDeletedFalse(email);
    }
}
