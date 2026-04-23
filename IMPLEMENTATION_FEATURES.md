# Expense Tracker - Implementation Features Documentation

## Overview
Expense Tracker is a RESTful API for managing personal expenses with category tracking, built with Spring Boot 3.x, PostgreSQL, and JWT authentication.

---

## Core Features Implemented

### 1. Authentication & Authorization
**Endpoints:** `/api/v1/auth/*`

#### Features:
- **User Registration** (`POST /api/v1/auth/register`)
  - Email validation with format checking
  - Password strength validation (8-15 chars, uppercase, lowercase, digit, special char)
  - BCrypt password hashing (strength 12)
  - Duplicate email prevention
  - Returns JWT access token (15-minute expiry)

- **User Login** (`POST /api/v1/auth/login`)
  - Email + password authentication
  - BCrypt password comparison
  - JWT token generation with user claims
  - Returns access token

#### Security Implementation:
- Stateless JWT authentication
- All endpoints except `/api/v1/auth/**` require valid JWT token
- Token passed via `Authorization: Bearer <token>` header
- User ID extracted from JWT claims (never trusted from request body)
- Password fields never logged or returned in responses
- CORS enabled with explicit allowed origins
- CSRF disabled (stateless REST API)

---

### 2. Category Management
**Endpoints:** `/api/v1/categories/*`

#### Features:
- **Create Category** (`POST /api/v1/categories`)
  - User-owned categories with unique name per user
  - Auto-generated category key (uppercase + underscore format)
  - Validates name length (1-100 chars)
  - Validates key uniqueness
  - Returns created category with metadata

- **List All Categories** (`GET /api/v1/categories`)
  - Returns system categories + user-owned categories
  - Paginated results (default: 20 items per page)
  - Sortable by any field (default: id DESC)
  - Custom `PageableRequest` DTO with validation
  - Returns `PaginatedResponse` with pagination metadata

- **Get Category by ID** (`GET /api/v1/categories/{id}`)
  - Returns single category if accessible by user
  - 404 if not found or not accessible

- **Update Category** (`PUT /api/v1/categories/{id}`)
  - Only user-owned categories can be updated
  - System categories are read-only
  - Validates name and key uniqueness
  - Returns updated category

- **Delete Category** (`DELETE /api/v1/categories/{id}`)
  - Soft delete only (sets `is_deleted = true`)
  - Only user-owned categories can be deleted
  - System categories are read-only
  - Returns success message

#### Business Rules:
- System categories (`user_id = NULL`) are accessible to all users but cannot be modified
- User-created categories are only accessible by the owner
- Category names must be unique per user (case-insensitive)
- Category keys must be unique per user
- Soft delete prevents permanent data loss

---

### 3. Expense Management
**Endpoints:** `/api/v1/expenses/*`

#### Features:
- **Create Expense** (`POST /api/v1/expenses`)
  - Records expense with amount, description, date, and categories
  - Amount validation (min: 1, stored as `NUMERIC(19,4)`)
  - Description validation (1-255 chars)
  - Date validation (must be past or present)
  - Category IDs validation (at least one, all must exist and be accessible)
  - Creates expense-category mappings in join table
  - Returns expense with associated categories

- **List All Expenses** (`GET /api/v1/expenses`)
  - Returns all expenses for authenticated user
  - Paginated results with custom page metadata
  - Sortable by any field
  - Each expense includes associated categories

- **Get Expense by ID** (`GET /api/v1/expenses/{id}`)
  - Returns single expense with categories
  - Access control: only owner can view
  - 404 if not found
  - 403 if user doesn't own the expense

- **Update Expense** (`PUT /api/v1/expenses/{id}`)
  - Updates amount, description, date, and categories
  - Access control: only owner can update
  - Removes old category mappings and creates new ones
  - Validates all fields (same as create)
  - Returns updated expense

- **Delete Expense** (`DELETE /api/v1/expenses/{id}`)
  - Soft delete of expense and its category mappings
  - Access control: only owner can delete
  - Sets `is_deleted = true` on expense and mappings
  - Returns success message

#### Business Rules:
- All expenses belong to a specific user (owner)
- Users can only access their own expenses
- Expenses must have at least one category
- Categories must exist and be accessible (system or user-owned)
- Date cannot be in the future
- Amount must be positive
- Soft delete preserves history

---

### 4. Database Design

#### Tables:
1. **users**
   - `id` (PK), `email` (unique), `name`, `password` (BCrypt hash)
   - `created_at`, `updated_at`, `is_deleted`

2. **categories**
   - `id` (PK), `name`, `category_key` (unique per user), `user_id` (FK, nullable for system categories)
   - `created_at`, `updated_at`, `is_deleted`
   - Unique constraint: `(user_id, category_key)` and `(user_id, name)`

3. **expenses**
   - `id` (PK), `user_id` (FK), `amount` (NUMERIC(19,4)), `description`, `expense_date`
   - `created_at`, `updated_at`, `is_deleted`

4. **expense_category_map** (Join table)
   - `id` (PK), `expense_id` (FK), `category_id` (FK)
   - `created_at`, `updated_at`, `is_deleted`
   - Unique constraint: `(expense_id, category_id)`

#### Database Standards:
- PostgreSQL as the primary database
- All tables have audit fields: `created_at`, `updated_at`, `is_deleted`
- All timestamps stored in UTC using `TIMESTAMPTZ`
- All monetary values use `NUMERIC(19, 4)` (never floating-point)
- Flyway for all schema migrations (versioned, immutable)
- HikariCP connection pool (min: 5, max: 20 connections)
- Indexes on all foreign keys
- **NO CASCADE DELETE** on any foreign key (all deletes handled explicitly in service layer)
- Soft delete for all tables

---

### 5. API Standards

#### Request/Response Format:
- **All requests**: JSON body with `Content-Type: application/json`
- **All responses**: Wrapped in `ApiResponse<T>` with:
  ```json
  {
    "success": true|false,
    "message": "Localized success/error message",
    "data": {...},           // only on success
    "errorCode": "ET-XXXX",  // only on error
    "timestamp": "2026-04-23T12:00:00"
  }
  ```

#### Pagination Response:
```json
{
  "data": [...],
  "pagination": {
    "current_page": 1,       // 1-indexed for client
    "page_size": 20,
    "total_items": 145,
    "total_pages": 8,
    "has_next": true,
    "links": {
      "next": "/api/v1/items?page=2&size=20",
      "prev": null
    }
  }
}
```

#### HTTP Status Codes:
- `200 OK` - Successful GET/PUT requests
- `201 Created` - Successful POST requests
- `204 No Content` - Successful DELETE requests (optional, currently returns 200)
- `400 Bad Request` - Validation errors
- `401 Unauthorized` - Missing or invalid JWT token
- `403 Forbidden` - Access denied (resource belongs to another user)
- `404 Not Found` - Resource not found
- `409 Conflict` - Duplicate resource (email, category name/key)
- `500 Internal Server Error` - Unexpected server errors

---

### 6. Error Handling

#### Error Code Ranges:
- `ET-1xxx`: Authentication/Authorization errors
- `ET-2xxx`: Expense errors
- `ET-3xxx`: Category errors
- `ET-4xxx`: Validation errors
- `ET-5xxx`: Database errors
- `ET-9xxx`: System errors

#### Key Error Codes:
| Code | Description |
|------|-------------|
| ET-1001 | User not found |
| ET-1002 | Invalid credentials |
| ET-1003 | Token expired |
| ET-1004 | Token invalid |
| ET-1005 | Email already exists |
| ET-2001 | Expense not found |
| ET-2004 | No categories provided |
| ET-2005 | Access denied to expense |
| ET-3001 | Category not found |
| ET-3002 | Duplicate category name/key |
| ET-3003 | Access denied to category |
| ET-4001 | General validation failure |
| ET-5001 | Generic database error |
| ET-9001 | Internal server error |

#### Exception Handling:
- `GlobalExceptionHandler` catches all exceptions
- `AppException` for typed application errors
- `ResourceNotFoundException` for 404 cases
- `ValidationException` for validation failures
- All validation errors collected and returned with field names
- Stack traces never exposed in API responses
- All errors logged with correlation ID

---

### 7. Internationalization (i18n)

#### Supported Languages:
- English (`en`) - default
- Hindi (`hi`)
- Telugu (`te`)

#### Implementation:
- Locale resolved from `Accept-Language` HTTP header
- `MessageSource` with property files in `i18n/` folder
- All success/error messages use i18n keys
- Validation messages use i18n keys
- Fallback to English if key not found
- UTF-8 encoding for all message files

#### Message Files:
- `messages.properties` (English)
- `messages_hi.properties` (Hindi)
- `messages_te.properties` (Telugu)

---

### 8. Logging

#### Implementation:
- SLF4J + Logback for logging
- Structured key-value logging: `event=action key1=value1 key2=value2`
- Correlation ID in MDC for request tracking
- `AppLogger` utility for consistent structured logs
- All log entries include correlation ID

#### Log Levels:
- `DEBUG` - SQL queries (dev only)
- `INFO` - Normal application flow (service entry/exit, controller actions)
- `WARN` - Degraded performance, 4xx client errors
- `ERROR` - 5xx server errors with full exception

#### Log Configuration:
- Current log: `logs/expensetracker.log` (always plain text, never compressed)
- Archived logs: `logs/expensetracker.YYYY-MM-DD.log.gz` (compressed)
- Retention: 30 days
- Max size: 1GB total for all archived logs
- Async appender for performance

#### Sensitive Data:
- Passwords NEVER logged
- JWT tokens NEVER logged
- Raw request bodies with sensitive data NEVER logged
- Only safe fields logged (amount, id, email, etc.)

---

### 9. Validation

#### Request Validation:
- Bean Validation (JSR-380) with annotations
- `@Valid` on all controller request parameters
- Custom validators (e.g., `@ValidSortDirection`)
- All validation messages reference i18n property keys

#### Field Validations:
**Auth:**
- Email: `@NotBlank`, `@Email`, `@Size(max=55)`
- Password: `@NotBlank`, `@Size(min=8, max=15)`, `@Pattern` (complexity rules)

**Category:**
- Name: `@NotBlank`, `@Size(min=1, max=100)`
- Key: `@NotBlank`, `@Size(min=1, max=50)`, uppercase + underscore pattern

**Expense:**
- Amount: `@NotNull`, `@DecimalMin("1")`, `@Digits(integer=15, fraction=4)`
- Description: `@NotBlank`, `@Size(min=1, max=255)`
- Date: `@NotNull`, `@PastOrPresent`
- CategoryIds: `@NotEmpty`

**Pagination:**
- Page: `@Min(0)`
- Size: `@Min(1)`
- Sort: `@Size(max=50)`
- Direction: `@ValidSortDirection` (ASC or DESC only, case-insensitive)

---

### 10. API Documentation (Swagger/OpenAPI)

#### Access:
- Swagger UI: `http://localhost:5894/swagger-ui.html`
- OpenAPI JSON: `http://localhost:5894/v3/api-docs`

#### Features:
- Interactive API testing
- Request/response examples for all endpoints
- All HTTP status codes documented
- Authentication scheme documented (Bearer JWT)
- `@Tag`, `@Operation`, `@ApiResponse` annotations on all endpoints
- Example values for all request/response fields
- `@ParameterObject` for pagination to avoid duplication

#### Groups:
- Auth - Authentication endpoints
- Categories - Category management
- Expenses - Expense management

---

## Non-Functional Features

### Performance:
- HikariCP connection pooling
- Paginated queries (no unbounded result sets)
- Async logging appender
- JPA `@Transactional` for transaction boundaries
- Read-only transactions for queries

### Security:
- BCrypt password hashing (strength 12)
- JWT token-based stateless auth
- Token expiry (15 minutes)
- User ownership validation on all resources
- userId extracted from JWT, never from request body
- HTTPS required in production
- Rate limiting (configured at gateway level)

### Maintainability:
- Layered architecture (Controller → Service → Repository)
- Constructor injection (no field injection)
- DTOs separate from entities
- MapStruct for entity-DTO mapping
- Comprehensive error handling
- Extensive inline documentation
- CLAUDE.md rules governing all code

### Testability:
- Service layer isolated with interfaces
- Repositories extend `JpaRepository`
- Constructor injection enables easy mocking
- DTOs are POJOs

---

## Technology Stack

### Core:
- **Java**: 17+
- **Spring Boot**: 3.2.5
- **Spring Data JPA**: For database access
- **Hibernate ORM**: 6.4.4
- **PostgreSQL**: Primary database
- **Maven**: Build tool

### Security:
- **Spring Security**: Authentication/authorization framework
- **JJWT**: JWT token generation and validation
- **BCrypt**: Password hashing

### Documentation:
- **Springdoc OpenAPI**: 3.x (Swagger UI)

### Logging:
- **SLF4J**: Logging facade
- **Logback**: Logging implementation

### Database Migration:
- **Flyway**: Schema versioning and migration

### Utilities:
- **Lombok**: Boilerplate code reduction
- **MapStruct**: Bean mapping

---

## Configuration

### Application Profiles:
- `default` - Development profile
- `dev` - Development with detailed logging
- `prod` - Production with optimized settings

### Key Configuration Properties:
```properties
# Server
server.port=5894

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/expensetracker
spring.datasource.hikari.pool-name=ExpenseTrackerHikariPool
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5

# JPA
spring.jpa.show-sql=false
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.open-in-view=false

# Flyway
spring.flyway.enabled=true
spring.flyway.validate-on-migrate=true

# Logging
logging.level.com.expensetracker=INFO
logging.level.org.hibernate.SQL=DEBUG
```

---

## Deployment

### Prerequisites:
- Java 17 or higher
- PostgreSQL 12 or higher
- Maven 3.6+

### Build:
```bash
mvn clean package
```

### Run:
```bash
java -jar target/expense-tracker-1.0.0.jar
```

### Database Setup:
1. Create database: `CREATE DATABASE expensetracker;`
2. Flyway migrations run automatically on startup
3. Default categories are seeded via migration

---

## Future Enhancements

### Planned Features:
- Expense filtering by date range
- Expense filtering by category
- Budget tracking per category
- Recurring expense templates
- Multi-currency support
- Expense attachments (receipts)
- Export to CSV/Excel
- Monthly/yearly expense reports
- Expense sharing between users
- Email notifications
- Refresh token support
- Password reset functionality

### Performance Optimizations:
- Redis caching for categories
- Database query optimization
- Batch operations for bulk inserts
- Pagination cursor support

### Security Enhancements:
- Two-factor authentication (2FA)
- Account lockout after failed attempts
- Password complexity policies
- Session management
- IP whitelisting

---

## Support & Maintenance

### Logs Location:
- Current log: `logs/expensetracker.log`
- Archived logs: `logs/expensetracker.YYYY-MM-DD.log.gz`

### Health Check:
- Endpoint: `/actuator/health`
- Returns: `{"status": "UP"}` if application is healthy

### Monitoring:
- Actuator endpoints enabled in production (with authentication)
- Correlation IDs for request tracing
- Structured logging for log aggregation

---

**Last Updated**: April 23, 2026  
**Version**: 1.0.0  
**Author**: Expense Tracker Team
