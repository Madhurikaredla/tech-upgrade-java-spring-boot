

package com.expensetracker.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

/**
 * User — represents a registered application user.
 *
 * DB table : users
 * PK       : id
 * Unique   : email (enforced by uq_users_email constraint)
 *
 * Security note: the password field holds a BCrypt hash (strength 12).
 * Raw passwords are NEVER stored or logged.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class User {

    /** Surrogate primary key — auto-incremented by the database sequence. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    /**
     * Timestamp of record creation.
     * Set automatically by Spring Data auditing on INSERT; never modified afterward.
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp of the most recent update.
     * Set automatically by Spring Data auditing on every UPDATE.
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Soft-delete flag.
     * Set to true by the service layer instead of issuing a SQL DELETE.
     * All queries MUST include WHERE is_deleted = false.
     * Defaults to false — must never be null.
     */
    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    /**
     * User's display name.
     * Maps to users.name VARCHAR(55).
     */
    @NotBlank
    @Size(max = 55)
    @Column(name = "name", nullable = false, length = 55)
    private String name;

    /**
     * User's login identifier — must be unique across all accounts.
     * Maps to users.email VARCHAR(55).
     */
    @NotBlank
    @Email
    @Size(max = 55)
    @Column(name = "email", nullable = false, unique = true, length = 55)
    private String email;

    /**
     * BCrypt-hashed password (strength 12).
     * NEVER expose this field in any response DTO or log line.
     * Maps to users.password VARCHAR(15).
     */
    @NotBlank
    @Size(max = 255)
    @Column(name = "password", nullable = false, length = 255)
    private String password;
}
