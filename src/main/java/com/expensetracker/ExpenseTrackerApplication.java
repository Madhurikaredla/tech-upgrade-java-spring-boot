package com.expensetracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ExpenseTrackerApplication — entry point for the Expense Tracker Spring Boot application.
 *
 * The application uses:
 *  - Spring Boot 3.x auto-configuration
 *  - Spring Data JPA + Hibernate (PostgreSQL)
 *  - Flyway for schema migrations (no ddl-auto create/update in production)
 *  - Spring Security with stateless JWT authentication
 *  - Springdoc OpenAPI 3.0 for API documentation
 *  - Lombok + MapStruct for boilerplate reduction and entity-DTO mapping
 *
 * For JPA auditing (@CreatedDate / @LastModifiedDate) see JpaConfig.
 */
@SpringBootApplication
public class ExpenseTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExpenseTrackerApplication.class, args);
    }
}
