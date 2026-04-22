package com.expensetracker.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Category — represents an expense category (e.g., Food, Transport, Entertainment).
 *
 * DB table : categories
 * PK       : id
 * Unique   : (user_id, name) for user-defined categories; enforced by uq_categories_user_name constraint
 *
 * System vs User Categories:
 *   - System categories: userId is NULL (seeded via Flyway, available to all users)
 *   - User-defined categories: userId is NOT NULL (specific to one user)
 *
 * All queries must filter WHERE is_deleted = false.
 * Users can only access their own categories and system categories (userId IS NULL).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "categories")
@EntityListeners(AuditingEntityListener.class)
public class Category {

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
     * Category name (e.g., "Food", "Transport").
     * Maps to categories.name VARCHAR(50).
     * Must be unique within the scope of a user (or globally for system categories where user_id IS NULL).
     */
    @NotBlank
    @Size(max = 50)
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    /**
     * Unique key for the category (e.g., "FOOD", "TRANSPORT").
     * Used for programmatic identification and i18n lookups.
     * Maps to categories.category_key VARCHAR(50).
     * Must be unique globally (enforced by uq_categories_key constraint).
     * Should be UPPER_SNAKE_CASE by convention.
     */
    @NotBlank
    @Size(max = 50)
    @Column(name = "category_key", nullable = false, length = 50, unique = true)
    private String categoryKey;

    /**
     * Owner of this category.
     * NULL for system-level categories (available to all users).
     * NOT NULL for user-defined categories (specific to one user).
     * Maps to categories.user_id BIGINT.
     */
    @Column(name = "user_id")
    private Long userId;
}
