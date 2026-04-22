# .claude.md — Expense Tracker Java Spring Boot Project Rules

> This file governs ALL code generation, architecture, naming, DB standards,
> error handling, scripts, and internationalization for this project.
> Claude MUST follow every rule here without exception.
> This file contains RULES ONLY — no code examples.

---

## 1. PROJECT OVERVIEW

- Project: Expense Tracker System
- Stack: Java 17+, Spring Boot 3.x, Spring Data JPA, Hibernate ORM, PostgreSQL
- Build Tool: Maven
- API Style: RESTful JSON APIs, versioned under `/api/v1`
- Auth: JWT-based stateless authentication
- Documentation: Swagger/OpenAPI 3.0 via Springdoc
- Logging: SLF4J + Logback with structured JSON output in production
- Internationalization: Spring MessageSource with Accept-Language header resolution
- DB Migrations: Flyway only
- Maintain inline docs and functional and terminology comments

---

## 2. PROJECT FOLDER STRUCTURE

```
expense-tracker/
├── src/
│   ├── main/
│   │   ├── java/com/expensetracker/
│   │   │   ├── ExpenseTrackerApplication.java
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── SwaggerConfig.java
│   │   │   │   ├── MessageSourceConfig.java
│   │   │   │   └── JpaConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── ExpenseController.java
│   │   │   │   └── CategoryController.java
│   │   │   ├── service/
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── ExpenseService.java
│   │   │   │   └── CategoryService.java
│   │   │   ├── repository/
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── ExpenseRepository.java
│   │   │   │   ├── ExpenseCategoryMapRepository.java
│   │   │   │   └── CategoryRepository.java
│   │   │   ├── entity/
│   │   │   │   ├── BaseEntity.java
│   │   │   │   ├── User.java
│   │   │   │   ├── Expense.java
│   │   │   │   ├── Category.java
│   │   │   │   └── ExpenseCategoryMap.java
│   │   │   ├── dto/
│   │   │   │   ├── request/
│   │   │   │   │   ├── RegisterRequest.java
│   │   │   │   │   ├── LoginRequest.java
│   │   │   │   │   ├── ExpenseRequest.java
│   │   │   │   │   └── CategoryRequest.java
│   │   │   │   └── response/
│   │   │   │       ├── ApiResponse.java
│   │   │   │       ├── AuthResponse.java
│   │   │   │       ├── ExpenseResponse.java
│   │   │   │       └── CategoryResponse.java
│   │   │   ├── mapper/
│   │   │   │   ├── ExpenseMapper.java
│   │   │   │   └── CategoryMapper.java
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── AppException.java
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   ├── ValidationException.java
│   │   │   │   └── ErrorCode.java
│   │   │   ├── security/
│   │   │   │   ├── JwtTokenProvider.java
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   └── UserDetailsServiceImpl.java
│   │   │   ├── scheduler/
│   │   │   │   └──
│   │   │   └── util/
│   │   │       ├── DateUtils.java
│   │   │       └── SecurityUtils.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       ├── db/migration/
│   │       │   ├── V1__create_users_table.sql
│   │       │   ├── V2__create_categories_table.sql
│   │       │   ├── V3__create_expenses_table.sql
│   │       │   ├── V4__create_expense_category_map_table.sql
│   │       │   ├── V5__seed_default_categories.sql
│   │       │   └── V6__create_indexes.sql
│   │       └── i18n/
│   │           ├── messages.properties
│   │           ├── messages_hi.properties
│   │           └── messages_te.properties
│   └── test/
│       └── java/com/expensetracker/
│           ├── controller/
│           ├── service/
│           └── repository/
├── scripts/
│   ├── db/
│   │   ├── backup.sh
│   │   ├── restore.sh
│   │   └── archive-expenses.sh
│   ├── deploy/
│   │   ├── start.sh
│   │   ├── stop.sh
│   │   └── health-check.sh
│   └── dev/
│       ├── setup-local.sh
│       └── run-migrations.sh
└── pom.xml
```

---

## 3. NAMING CONVENTIONS

### Java Naming

- Classes: PascalCase — `ExpenseService`, `GlobalExceptionHandler`
- Methods: camelCase — `findExpenseById`, `createExpense`
- Variables: camelCase — `expenseDate`, `categoryIds`
- Constants: UPPER_SNAKE_CASE — `MAX_RETRY_COUNT`, `JWT_EXPIRY_SECONDS`
- Packages: lowercase, dot-separated — `com.expensetracker.service`
- Request DTOs: `{Entity}Request` — `ExpenseRequest`, `LoginRequest`
- Response DTOs: `{Entity}Response` — `ExpenseResponse`, `CategoryResponse`
- Repositories: `{Entity}Repository` — `ExpenseRepository`
- Services: `{Entity}Service` — `ExpenseService`
- Controllers: `{Entity}Controller` — `ExpenseController`
- Mappers: `{Entity}Mapper` — `ExpenseMapper`
- Exception classes: full words — `ResourceNotFoundException`, not `ResourceNotFoundExc`
- Test classes: `{ClassName}Test` — `ExpenseServiceTest`
- Scheduler classes: `{Domain}Scheduler`

### File and Class Rules

- One top-level public class per file; filename must match the class name exactly.
- No abbreviations in class or method names.
- No wildcard imports (`import java.util.*` is forbidden).
- Never use `temp`, `test123`, or placeholder names in production code.

### Database Naming

- Table names: snake_case, plural — `expenses`, `expense_category_map`
- Column names: snake_case — `expense_date`, `user_id`, `is_deleted`
- Primary key column: always named `id`
- Foreign key columns: `{referenced_table_singular}_id` — `user_id`, `category_id`
- Index names: `idx_{table}_{column(s)}` — `idx_expenses_user_id`
- Unique constraint names: `uq_{table}_{column(s)}` — `uq_users_email`
- Check constraint names: `chk_{table}_{column}` — `chk_expenses_amount_positive`
- FK constraint names: `fk_{child_table}_{parent_table}` — `fk_expenses_users`
- Migration files: `V{n}__{description_in_snake_case}.sql`

---

## 4. LAYER RESPONSIBILITIES

### Controller Layer
- Accepts HTTP requests, validates input with `@Valid`, delegates to service, returns response.
- NEVER contains business logic.
- NEVER contains `@Transactional`.
- NEVER accesses repositories directly.
- NEVER returns entity objects — always return response DTOs wrapped in `ApiResponse<T>`.
- Must annotate every endpoint with Swagger `@Tag`, `@Operation`, and all `@ApiResponse` codes.
- All endpoints must declare their HTTP status codes explicitly.

### Service Layer
- Owns all business logic.
- Owns all transaction boundaries (`@Transactional` or `@Transactional(readOnly = true)`).
- NEVER returns entity objects to the controller — always maps to DTOs before returning.
- Must log entry and exit of every significant operation.
- Must throw typed `AppException` with a specific `ErrorCode` — never raw `RuntimeException`.
- Enforces user ownership checks — always verify `userId` before operating on any resource.
- Extracts `userId` from the security context via `SecurityUtils` — never from the request body.

### Repository Layer
- NEVER contains business logic.
- All list queries must use `Pageable` — no unbounded `findAll()`.
- All queries must filter `isDeleted = false`.
- All user-scoped queries must filter by `userId`.
- JPQL for custom queries; native SQL only when JPQL is insufficient (with a comment explaining why).

### Entity Layer
- NEVER exposed outside the service layer.
- NEVER passed to controllers or returned in responses.
- All entities extend `BaseEntity`.

### DTO Layer
- Request DTOs carry all validation annotations.
- Response DTOs carry all Swagger `@Schema` annotations.
- DTOs are pure data containers — no business logic, no JPA annotations.
- MapStruct mappers handle all entity-to-DTO and DTO-to-entity conversions.

---

## 5. ENTITY STANDARDS

### BaseEntity Rules

### Entity Rules (No BaseEntity)
Entities do NOT need to extend a common `BaseEntity`.
Each entity must declare its own primary key (`id`) and audit fields (`createdAt`, `updatedAt`, `isDeleted`) as needed.
`createdAt` is non-updatable, set automatically via `@CreatedDate` if present.
`updatedAt` is set automatically via `@LastModifiedDate` if present.
`isDeleted` defaults to `false` and must never be `null` if used.


### Entity Layer
NEVER exposed outside the service layer.
NEVER passed to controllers or returned in responses.
Entities do NOT need to extend `BaseEntity`.
- Use `LocalDate` for date-only fields; use `LocalDateTime` for timestamps.

### Entity Field Rules
Always declare `@Column(name = "...")` explicitly on every field.
Always declare `@Table(name = "...")` explicitly on every entity.
Use `LocalDate` for date-only fields; use `LocalDateTime` for timestamps.
NEVER use `java.util.Date`, `java.sql.Date`, or `java.sql.Timestamp`.
Use `BigDecimal` for all monetary amounts.
NEVER use `double`, `float`, `Double`, or `Float` for money.
Annotate fields with `@NotNull`, `@Size`, `@Min`, `@Max`, `@DecimalMin`, `@DecimalMax` where constraints apply.
String columns must have explicit length limits — never map a user-input field to an unbounded type.
If audit fields (`createdAt`, `updatedAt`, `isDeleted`) are needed, declare them in each entity as appropriate.
- NEVER use `CascadeType.ALL` on any relationship.
- NEVER use `CascadeType.REMOVE` on any relationship.
- NEVER use `orphanRemoval = true` on ManyToMany-style relationships.
- Cascade only `PERSIST` and `MERGE` where genuinely necessary; document the reason in a comment.
- NEVER use bare `@ManyToMany` with `@JoinTable` directly on entities — always create an explicit join entity with its own `id` and audit fields.
- Join entity (`ExpenseCategoryMap`) must have a `UNIQUE` constraint on `(expense_id, category_id)`.

### Soft Delete Rules
- All deletes are soft deletes: set `isDeleted = true`, never issue a SQL `DELETE`.
- All queries must include `WHERE is_deleted = false`.
- Soft delete is applied in the service layer — never bypass via direct repository calls.
- Soft-deleted records must not be retrievable by any public API endpoint.

---

## 6. DATABASE STANDARDS

### Schema Standards
- PostgreSQL is the only supported database.
- Every table must have columns: `id`, `created_at`, `updated_at`, `is_deleted`.
- All timestamps stored in UTC using `TIMESTAMPTZ`.
- All monetary columns use `NUMERIC(19, 4)` — NEVER `FLOAT`, `REAL`, or `DOUBLE PRECISION`.
- All user-input string columns use `VARCHAR(n)` with an explicit length — NEVER unbounded `TEXT`.
- Every `NOT NULL` column must have a `DEFAULT` or be guaranteed non-null at the application level.
- Every table must have appropriate `CHECK` constraints (e.g., `amount > 0`).

### CASCADE DELETE — STRICTLY FORBIDDEN
- NEVER add `ON DELETE CASCADE` to any foreign key constraint in any migration file.
- NEVER add `ON DELETE SET NULL` without explicit approval documented in the migration comment.
- NEVER use `CascadeType.REMOVE` in any JPA entity mapping.
- NEVER use `CascadeType.ALL` in any JPA entity mapping.
- All deletions are handled explicitly in the service layer via soft delete only.
- The database must never automatically delete or nullify related rows because a parent row was changed.
- This rule applies to ALL tables without exception, including `expense_category_map`.

### Foreign Key Standards
- Every foreign key column must have a corresponding `CREATE INDEX` statement.
- Foreign keys reference the `id` column of the parent table.
- FK constraint names follow `fk_{child_table}_{parent_table}`.
- FK constraints must be defined in the same migration that creates the child table.

### Index Standards
- Every foreign key column must be indexed.
- Composite indexes must be created for columns frequently queried together (e.g., `user_id + expense_date`).
- Index names follow `idx_{table}_{column(s)}`.
- All indexes defined in a dedicated migration `V6__create_indexes.sql` using separate `CREATE INDEX` statements.
- NEVER create indexes inline inside `CREATE TABLE` statements.

### Normalization Rules
- All tables must be in Third Normal Form (3NF) minimum.
- No repeating groups within a single row.
- No partial key dependencies.
- No transitive dependencies — non-key columns depend only on the PK, not on other non-key columns.
- Lookup and master data (default categories) live in dedicated tables, seeded via Flyway.
- System-level categories use `user_id = NULL`; user-defined categories use `user_id = {userId}`.

### Flyway Migration Standards
- All schema changes go through Flyway — NEVER use `ddl-auto=create`, `update`, or `create-drop` in any non-test environment.
- Migration file naming: `V{n}__{description_in_snake_case}.sql` with sequential `n`.
- NEVER modify or delete an already-applied migration file.
- Each migration includes a comment header describing its purpose.
- Each migration includes a rollback script as a SQL comment block at the bottom.
- Use `IF NOT EXISTS` / `IF EXISTS` for idempotent operations where possible.
- `spring.flyway.validate-on-migrate=true` must always be enabled.
- Seed data goes in its own migration: `V5__seed_default_categories.sql`.

### Connection Pool Standards (HikariCP)
- HikariCP is the only connection pool to use.
- `minimum-idle` = 5
- `maximum-pool-size` = 20
- `connection-timeout` = 20000 ms
- `idle-timeout` = 300000 ms
- `max-lifetime` = 1200000 ms
- Pool name must be set to `ExpenseTrackerHikariPool`.
- Pool settings must be in `application-prod.yml`.
- `DataAccessException` must always be caught in `GlobalExceptionHandler` and mapped to `ErrorCode.DB_ERROR`.
- NEVER catch and silently swallow `SQLException` or `DataAccessException`.
- NEVER access a datasource directly — always go through Spring Data JPA repositories.

### Transaction Standards
- `@Transactional(readOnly = true)` on all service methods that only read data.
- `@Transactional` (read-write) only on service methods that write data.
- Transaction boundaries belong in the Service layer ONLY.
- NEVER put `@Transactional` on controllers.
- NEVER put `@Transactional` on `private` methods — Spring AOP cannot proxy them.
- NEVER open a transaction that spans an HTTP request boundary.

### Data Security Standards
- All passwords stored as BCrypt hashes with strength 12 — NEVER plaintext.
- JWT secrets must NEVER be set in any code 
- All endpoints except `/api/v1/auth/**` require `Authorization: Bearer <token>`.
- `userId` always extracted from the JWT token — NEVER trusted from the request body.
- Users can only access their own resources — always filter by `userId` in service layer.
- Sensitive fields (`password`, tokens) must NEVER appear in any response DTO.
- NEVER log passwords, tokens, or raw sensitive request bodies.
- HTTPS required in production — HTTP traffic is rejected or redirected.
- CORS: explicit allowed origins only — `allowedOrigins("*")` is forbidden in production.
- Rate limiting applied at gateway or Spring filter level.

### Trigger Standards
- DB triggers are permitted only for audit logging — never for business logic.
- Each trigger defined in its own migration file with a descriptive comment header.
- Prefer Spring `@CreatedDate` / `@LastModifiedDate` auditing over DB triggers wherever possible.
- Document every trigger's purpose, table, and event in the migration comment.

### DB Connection Handling Standards
- Always use Spring Data JPA repositories — never raw JDBC unless JPQL cannot express the query.
- If JDBC template is used, document the reason in a code comment.
- Connection acquisition timeout is 20 seconds — surface as `ErrorCode.DB_CONNECTION_FAILED`.
- Never perform DB operations outside a Spring-managed transaction where data integrity is required.

---

## 7. SCRIPTS STANDARDS

All scripts live in the `scripts/` directory at the project root. All scripts are Bash.

### General Script Rules (apply to all scripts)
- Include `set -euo pipefail` at the top of every script.
- Include a usage/help comment block at the top of every script.
- Read all configuration (DB host, port, password, paths) from environment variables — never hardcode.
- Log to stdout with timestamps on every significant step.
- Exit with non-zero code on any failure.
- All scripts must be executable (`chmod +x`).
- Never commit scripts with hardcoded credentials or connection strings.

### scripts/db/backup.sh
- Creates a timestamped PostgreSQL dump of the entire database.
- Stores backup in a directory configured by env var `BACKUP_DIR`.
- Retains only the last 7 daily backups — deletes older ones automatically.
- Logs success or failure with timestamp.

### scripts/db/restore.sh
- Accepts a backup file path as a required argument — exits with usage error if not provided.
- Validates the backup file exists before proceeding.
- Drops and recreates the target database before restoring.
- Logs each step of the restore process.
- Must be run manually only — never called from application code.

### scripts/db/archive-expenses.sh

- Requires DBA-level credentials provided via environment variables.
- Logs rows archived and total duration.
- Must be idempotent — safe to re-run without duplicating archived data.

### scripts/deploy/start.sh
- Starts the Spring Boot application with environment-specific JVM flags.
- Reads `APP_ENV` env var to select the correct `application-{env}.yml`.
- Sets all required JVM flags: heap size, GC settings, heap dump path.
- Polls the health endpoint (`/actuator/health`) after startup.
- Exits with non-zero code if health check fails within 30 seconds.

### scripts/deploy/stop.sh
- Sends `SIGTERM` to the running application and waits up to 30 seconds for graceful shutdown.
- Sends `SIGKILL` only if graceful shutdown does not complete within the timeout.
- Logs the stop event with timestamp.

### scripts/deploy/health-check.sh
- Polls `GET /actuator/health` at a configurable interval.
- Returns exit code 0 if the application reports UP.
- Returns exit code 1 if the application is DOWN or unreachable.
- Used by CI/CD pipelines and `start.sh` to verify deployment success.

### scripts/dev/setup-local.sh
- Validates prerequisites: Java 17, Maven, PostgreSQL.
- Creates the local development database if it does not already exist.
- Sets required environment variables for local development.
- Runs Flyway migrations on the local database.

### scripts/dev/run-migrations.sh
- Runs Flyway migrations against the target database.
- Reads DB connection details from environment variables only.
- Prints migration status: which migrations were applied, which are pending.
- Exits with non-zero code on migration failure.

---

## 8. CONTROLLER STANDARDS

- Version all API paths: `/api/v1/...`
- All responses wrapped in `ApiResponse<T>` with fields: `success`, `message`, `errorCode`, `data`, `timestamp`.
- All request DTOs annotated with `@Valid`.
- Constructor injection only — no `@Autowired` on fields.
- Every endpoint annotated with `@Operation` and all possible `@ApiResponse` codes.
- HTTP status code usage: 200 (GET/PUT success), 201 (POST create), 204 (DELETE), 400 (validation), 401 (auth), 403 (forbidden), 404 (not found), 409 (conflict), 500 (server error).

### Pagination Response Standards

ALL paginated endpoints MUST return responses in this exact structure:

```json
{
  "data": [ ... ],
  "pagination": {
    "current_page": 2,
    "page_size": 20,
    "total_items": 145,
    "total_pages": 8,
    "has_next": true,
    "links": {
      "next": "/api/v1/items?page=3&size=20",
      "prev": "/api/v1/items?page=1&size=20"
    }
  }
}
```

Rules:
- Use `PaginatedResponse<T>` DTO for all paginated endpoints
- `data` field contains the list of items for the current page
- `pagination` field contains `PaginationMetadata` with:
  - `current_page`: 1-indexed page number (page=0 from Pageable becomes current_page=1)
  - `page_size`: number of items per page
  - `total_items`: total count across all pages
  - `total_pages`: calculated total pages
  - `has_next`: boolean indicating if next page exists
  - `links.next`: URL to next page (null if on last page)
  - `links.prev`: URL to previous page (null if on first page)
- Page numbers in URLs use 0-indexed `page` parameter (Spring convention)
- Page numbers in response use 1-indexed `current_page` (user-friendly)
- NEVER return raw `Page<T>` from Spring Data — always convert to `PaginatedResponse<T>`

---

## 9. SERVICE STANDARDS

- Constructor injection only.
- All read-only methods annotated with `@Transactional(readOnly = true)`.
- All write methods annotated with `@Transactional`.
- Log entry of every significant operation at INFO level with relevant IDs.
- Log success at INFO level.
- Log all errors at ERROR level with full exception.
- NEVER return entity objects — always map to DTOs.
- NEVER throw `RuntimeException` directly — always throw `AppException` with an `ErrorCode`.
- Always extract `userId` from security context via `SecurityUtils` — never from the request body.

---

## 10. REPOSITORY STANDARDS

- All list queries must accept and use `Pageable`.
- `findAll()` without `Pageable` is forbidden.
- All queries must filter `isDeleted = false`.
- All user-scoped queries must filter by `userId`.
- Custom query method names must clearly describe what they query.
- JPQL preferred for custom queries; native SQL only when JPQL is insufficient, with a comment.

---

## 11. DTO STANDARDS


### Request DTOs
- Every required field must have `@NotNull` or `@NotBlank`.
- Every string field must have `@Size(max = n)` matching the DB column limit.
- Every numeric field must have `@DecimalMin` or `@Min` where applicable.
- All password fields must use a `@Pattern` enforcing at least 1 lowercase, 1 uppercase, 1 digit, 1 special character, and length 8–15. Message key: `validation.auth.password.pattern`.
- All email fields must use `@Email`, `@NotBlank`, and `@Size(max=55)`.
- All sort direction fields must use an enum (e.g., `SortDirection { ASC, DESC }`) and validate input to accept only these values, case-insensitive.
- All validation messages reference i18n property keys — never hardcoded strings.
- DTOs use Lombok `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`.

### Response DTOs
- Every field must have `@Schema(description = "...", example = "...")` for Swagger.
- NEVER include sensitive fields (`password`, raw tokens) in any response DTO.
- DTOs use Lombok `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`.

---

## 12. EXCEPTION & ERROR CODE STANDARDS

### ErrorCode Ranges
- Auth: `ET-1001` to `ET-1099`
- Expense: `ET-2001` to `ET-2099`
- Category: `ET-3001` to `ET-3099`
- Validation: `ET-4001` to `ET-4099`
- Database: `ET-5001` to `ET-5099`
- System: `ET-9001` to `ET-9099`

### Defined Error Codes

| Code | Description |
|---|---|
| ET-1001 | User not found |
| ET-1002 | Invalid credentials |
| ET-1003 | Token expired |
| ET-1004 | Token invalid |
| ET-1005 | Email already exists |
| ET-1006 | Unauthorized |
| ET-1007 | Forbidden |
| ET-2001 | Expense not found |
| ET-2002 | Invalid expense amount |
| ET-2003 | Expense date in the future |
| ET-2004 | No categories provided |
| ET-2005 | Access denied to expense |
| ET-3001 | Category not found |
| ET-3002 | Duplicate category name |
| ET-3003 | Access denied to category |
| ET-3004 | Duplicate category key |
| ET-4001 | General validation failure |
| ET-4002 | Required field missing |
| ET-4003 | Field size exceeded |
| ET-4004 | Invalid format |
| ET-5001 | Generic DB error |
| ET-5002 | DB constraint violation |
| ET-5003 | Duplicate DB entry |
| ET-5004 | DB connection failed |
| ET-9001 | Internal server error |
| ET-9002 | Service unavailable |
| ET-9003 | Rate limit exceeded |

### Exception Rules
- All custom exceptions extend `AppException`.
- `AppException` carries an `ErrorCode` and an `HttpStatus`.
- `ResourceNotFoundException` is used for all 404 cases — extends `AppException`.
- `GlobalExceptionHandler` handles: `AppException`, `MethodArgumentNotValidException`, `DataAccessException`, and all uncaught `Exception`.
- NEVER expose stack traces in API responses.
- NEVER swallow exceptions silently.
- NEVER throw a new exception inside a catch block without including the original cause.
- Error response always includes: `success: false`, `errorCode`, `message` (i18n resolved), `timestamp`.

---

## 13. INTERNATIONALIZATION (i18n) STANDARDS

### Configuration Rules
- Use `ReloadableResourceBundleMessageSource` with basename `classpath:i18n/messages`.
- Default encoding: UTF-8.
- `fallbackToSystemLocale` must be `false`.
- Locale resolved from `Accept-Language` HTTP header.
- Default locale: English (`en`).
- Supported locales: `en`, `hi`, `te`.

### Message Key Rules
- ALL error messages and validation messages use property keys — never hardcoded strings in Java.
- ALL success messages use property keys — never hardcoded strings in Java.
- Message keys follow: `success.{domain}.{action}` for success, `error.{domain}.{errorName}` for errors, `validation.{entity}.{field}.{rule}` for validation.
- NEVER return an unresolved message key to the client.
- All locale files must cover every key defined in the default `messages.properties`.
- Controllers must inject `MessageSource` and use `LocaleContextHolder.getLocale()` to resolve messages.

### Required Message Keys

Success messages:
- Auth: `success.auth.registered`, `success.auth.login`
- Category: `success.category.created`, `success.category.retrieved`, `success.category.list`, `success.category.updated`, `success.category.deleted`
- Expense: `success.expense.created`, `success.expense.retrieved`, `success.expense.list`, `success.expense.updated`, `success.expense.deleted`

Error messages:
Auth: `error.auth.userNotFound`, `error.auth.invalidCredentials`, `error.auth.tokenExpired`, `error.auth.tokenInvalid`, `error.auth.emailAlreadyExists`, `error.auth.unauthorized`, `error.auth.forbidden`

Expense: `error.expense.notFound`, `error.expense.invalidAmount`, `error.expense.dateInvalid`, `error.expense.categoryRequired`, `error.expense.accessDenied`

Category: `error.category.notFound`, `error.category.nameDuplicate`, `error.category.accessDenied`, `error.category.keyDuplicate`

Validation: `error.validation.failed`, `error.validation.fieldRequired`, `error.validation.fieldSizeExceeded`, `error.validation.invalidFormat`

Database: `error.db.generic`, `error.db.constraintViolation`, `error.db.duplicateEntry`, `error.db.connectionFailed`

System: `error.system.internalError`, `error.system.serviceUnavailable`, `error.system.rateLimitExceeded`

Validation messages:
Category validation: `validation.category.name.required`, `validation.category.name.size`, `validation.category.key.required`, `validation.category.key.size`

Expense validation: `validation.expense.amount.required`, `validation.expense.amount.min`, `validation.expense.amount.digits`, `validation.expense.description.required`, `validation.expense.description.size`, `validation.expense.date.required`, `validation.expense.date.pastOrPresent`, `validation.expense.categories.required`

---

## 14. LOGGING STANDARDS

- Always use SLF4J via Lombok `@Slf4j`. NEVER use `System.out.println`.
- All structured logging MUST go through `AppLogger` utility (`AppLogger.info`, `AppLogger.warn`, `AppLogger.error`) in both service AND controller layers — never call `log.info()`, `log.warn()`, or `log.error()` directly.
- Log format: structured key=value pairs — `event=expense.create userId=12 expenseId=42 amount=500.00`.
- Log levels: DEBUG (dev only), INFO (normal flow), WARN (degraded/slow queries, 4xx client errors), ERROR (5xx server failures with exception).
- Every inbound request must have a correlation ID set in MDC at the filter level.
- Correlation ID must appear in every log line for that request.
- Production Logback config must use Logstash JSON encoder.
- NEVER log: passwords, JWT tokens, raw sensitive request bodies.
- Log `userId` and resource `id` on every significant operation.

- Slow queries exceeding 500ms must be logged at WARN level.
- `NoResourceFoundException` (Spring MVC 404 for static resources like `/favicon.ico`, `/swagger-ui`) must be handled explicitly in `GlobalExceptionHandler` at DEBUG level — never at ERROR level.
- `AppException` with a 4xx HTTP status must be logged at WARN without stack trace. Only 5xx errors are logged at ERROR with stack trace.
- NEVER log the full request body — only log safe individual fields (e.g., `amount`, `id`, `email`). Never log passwords, tokens, or sensitive payloads.
- Controllers must import and use `AppLogger` — not raw `log.*` calls.

---

## 15. SWAGGER / OPENAPI STANDARDS

- All controllers annotated with `@Tag(name = "...", description = "...")`.
- All endpoints annotated with `@Operation(summary = "...")`.
- All possible response codes declared with `@ApiResponse`.
- All request DTO fields annotated with `@Schema(description = "...", example = "...")`.
- Swagger UI path: `/swagger-ui.html`.
- Swagger UI disabled in production profile via `springdoc.api-docs.enabled=false`.
- API groups: Auth, Expenses, Categories.
- `Accept-Language` header documented as a supported parameter on all endpoints.

---

## 16. SECURITY STANDARDS

- JWT secret: dont maintain in the code.
- JWT access token expiry: 15 minutes.
- JWT refresh token expiry: 7 days.
- Passwords: BCrypt with strength 12 — never store plaintext.
- All endpoints except `/api/v1/auth/**` require `Authorization: Bearer <token>`.
- `userId` always extracted from JWT — never trusted from the request body.
- CORS: explicit allowed origins only — `allowedOrigins("*")` is forbidden in production.
- CSRF: disabled (stateless REST with JWT).
- Rate limiting applied at gateway or Spring filter level.
- HTTPS required in production — HTTP traffic must be rejected or redirected.

---

## 17. TESTING STANDARDS

- Unit tests cover every service method using mocked repositories.
- Integration tests cover every controller endpoint using `@SpringBootTest` and Testcontainers.
- Minimum test coverage: 80%.
- Test naming: `should_{expectedBehavior}_when_{condition}`.
- All test methods annotated with `@DisplayName`.
- Test data must not depend on auto-increment IDs being predictable.
- NEVER use a production database for tests — always use Testcontainers with PostgreSQL.

---

## 18. MAVEN DEPENDENCY STANDARDS

Required runtime dependencies:
- `spring-boot-starter-web`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-security`
- `spring-boot-starter-validation`
- `spring-boot-starter-actuator`
- `spring-boot-starter-cache`
- `springdoc-openapi-starter-webmvc-ui` (3.x)
- `jjwt-api`, `jjwt-impl`, `jjwt-jackson`
- `flyway-core`, `flyway-database-postgresql`
- `postgresql` (scope: runtime)
- `lombok`
- `mapstruct`
- `logstash-logback-encoder`

Required test dependencies:
- `spring-boot-starter-test`
- `testcontainers` with `postgresql` module
- `spring-security-test`

---

## 19. ANTI-PATTERNS — NEVER DO

- `@Autowired` on fields — use constructor injection
- `spring.jpa.hibernate.ddl-auto=create`, `update`, or `create-drop` in production
- `ON DELETE CASCADE` on any foreign key — strictly forbidden
- `ON DELETE SET NULL` without documented approval
- `CascadeType.ALL` on any JPA relationship
- `CascadeType.REMOVE` on any JPA relationship
- `orphanRemoval = true` on ManyToMany-style relationships
-  Bare `@ManyToMany` — always use an explicit join entity
- `FetchType.EAGER` on any relationship
- `findAll()` without `Pageable`
-  Business logic in controllers
- SQL queries in service classes
- DB queries in controllers
- Catching and swallowing exceptions silently
- Throwing raw `RuntimeException` — always use `AppException` with `ErrorCode`
- Exposing stack traces in API responses
- Hardcoded user-facing strings — always use i18n message keys
- `double` or `float` for money — use `BigDecimal`
- `java.util.Date` or `java.sql.Date` — use `java.time.*`
- Returning entity objects from controllers or services
- `@Transactional` on controllers
- `@Transactional` on `private` methods
- Wildcard imports (`import java.util.*`)
- Logging passwords, tokens, or raw sensitive request bodies
- Trusting `userId` from request body — always extract from JWT
- `allowedOrigins("*")` in production CORS config
- Exposing `/actuator` endpoints without authentication in production
- `System.out.println` — always use SLF4J
- Hardcoded credentials or connection strings in any file or script
- Hardcoded cache names as magic strings — use constants
- Skipping `WHERE is_deleted = false` in any query
- Missing correlation ID in log lines
- Hard deleting any row — always soft delete
- Loading unbounded result sets into memory
- Performing bulk operations on the request thread