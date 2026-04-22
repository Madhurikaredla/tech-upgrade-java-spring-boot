-- =============================================================
-- V3__create_expenses_and_expense_category_map_tables.sql
-- Creates the expenses table and the expense_category_map join
-- table that enforces the many-to-many relationship between
-- expenses and categories.
--
-- Rules:
--   - No ON DELETE CASCADE on any FK — all deletes are soft deletes
--   - All monetary amounts stored as NUMERIC(19,4)
--   - All timestamps stored as TIMESTAMPTZ in UTC
--   - Soft-delete flag (is_deleted) on every table
-- =============================================================

-- Create expenses table
CREATE TABLE IF NOT EXISTS expenses (
    id          BIGSERIAL    PRIMARY KEY,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    is_deleted  BOOLEAN      NOT NULL DEFAULT FALSE,
    user_id     BIGINT       NOT NULL,
    amount      NUMERIC(19,4) NOT NULL,
    expense_date DATE         NOT NULL,
    description VARCHAR(255),
    CONSTRAINT chk_expenses_amount_positive CHECK (amount > 0),
    CONSTRAINT fk_expenses_users FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create expense_category_map table
-- Represents the many-to-many relationship between expenses and categories.
-- Explicit join entity with its own PK, audit fields, and soft-delete.
CREATE TABLE IF NOT EXISTS expense_category_map (
    id          BIGSERIAL   PRIMARY KEY,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted  BOOLEAN     NOT NULL DEFAULT FALSE,
    expense_id  BIGINT      NOT NULL,
    category_id BIGINT      NOT NULL,
    CONSTRAINT uq_expense_category UNIQUE (expense_id, category_id),
    CONSTRAINT fk_expense_category_map_expenses   FOREIGN KEY (expense_id)  REFERENCES expenses(id),
    CONSTRAINT fk_expense_category_map_categories FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- =============================================================
-- Rollback:
--   DROP TABLE IF EXISTS expense_category_map;
--   DROP TABLE IF EXISTS expenses;
-- =============================================================
