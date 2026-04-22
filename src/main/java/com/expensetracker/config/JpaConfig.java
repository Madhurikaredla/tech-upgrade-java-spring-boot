package com.expensetracker.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JpaConfig — enables Spring Data JPA auditing.
 *
 * @EnableJpaAuditing activates the AuditingEntityListener so that:
 *   - @CreatedDate  : sets BaseEntity.createdAt on INSERT
 *   - @LastModifiedDate : sets BaseEntity.updatedAt on every UPDATE
 *
 * Placing this in a dedicated config class (rather than the main application class)
 * keeps auditing setup isolated and testable without loading the full context.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
    // No additional beans — @EnableJpaAuditing is sufficient.
}
