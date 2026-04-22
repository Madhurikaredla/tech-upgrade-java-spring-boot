# Expense Tracker System — Specification

## 1. Problem Statement
Design and develop an Expense Tracker System that allows users to record, manage, and organize their expenses.

The system should:
- Allow users to create and manage expenses
- Allow categorization of expenses
- Support a many-to-many relationship between expenses and categories

---

## 2. Scope

### In Scope
- User registration and authentication
- CRUD operations for expenses
- CRUD operations for categories
- Expense ↔ Category mapping (M:N)

### Out of Scope
- Analytics
- Budget tracking
- Notifications
- Payment integrations

---

## 3. Functional Requirements

### User Management
- User can register
- User can log in

### Expense Management
- Create expense
- Update expense
- Delete expense
- View all expenses

Each expense contains:
- Amount
- Description
- Expense date

### Category Management
- Create category
- View categories
- Categories can be user-defined or default

### Expense-Category Mapping
- One expense can have multiple categories
- One category can belong to multiple expenses

---

## 4. System Architecture

### High-Level Architecture
Client → Backend API → Database

### Backend Layers
- Controller Layer
- Service Layer
- Repository Layer
- Entity Layer

---

## 5. Database Design

### User Table
```

id (PK)
name
email (unique)
password
created_at

```

### Category Table
```

id (PK)
name
user_id (FK, nullable)
created_at

```

### Expense Table
```

id (PK)
amount
description
expense_date
user_id (FK)
created_at

```

### Expense_Category_Map Table
```

id (PK)
expense_id (FK)
category_id (FK)

```

---

## 6. Relationships
- User → Expense = 1:N
- User → Category = 1:N
- Expense ↔ Category = M:N

---

## 7. API Specification

### Auth APIs
- POST /auth/register
- POST /auth/login

### Expense APIs
- POST /expenses
- GET /expenses
- GET /expenses/{id}
- PUT /expenses/{id}
- DELETE /expenses/{id}

### Category APIs
- POST /categories
- GET /categories

---

## 8. Example Request

### Create Expense
```

{
"amount": 500,
"description": "Dinner",
"expenseDate": "2026-04-18",
"categoryIds": [1, 2]
}

````

---

## 9. ORM Mapping (Example - Spring Boot)

```java
@ManyToMany
@JoinTable(
    name = "expense_category_map",
    joinColumns = @JoinColumn(name = "expense_id"),
    inverseJoinColumns = @JoinColumn(name = "category_id")
)
private List<Category> categories;
````

lsof -ti:5894 | xargs kill -9 2>/dev/null; echo "Port 5894 killed"