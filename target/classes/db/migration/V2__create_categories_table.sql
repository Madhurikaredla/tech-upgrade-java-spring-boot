-- ============================================================
-- Migration : V2__create_categories_table.sql
-- Purpose   : Creates the categories table for expense categorization.
--             Categories can be system-level (user_id IS NULL, available to all)
--             or user-defined (user_id NOT NULL, specific to one user).
-- ============================================================

CREATE TABLE IF NOT EXISTS categories (
    -- Primary key — auto-incremented by the database sequence
    id          BIGSERIAL       PRIMARY KEY,


    -- Category name (e.g., "Food", "Transport", "Entertainment")
    -- Must be unique within a user's scope (enforced by unique constraint below)
    name        VARCHAR(50)     NOT NULL,

    -- Unique key for the category (e.g., "FOOD", "TRANSPORT")
    -- Used for programmatic identification and i18n lookups
    category_key VARCHAR(50)    NOT NULL UNIQUE,

    -- Owner of this category
    -- NULL for system-level categories (available to all users)
    -- NOT NULL for user-defined categories (specific to one user)
    user_id     BIGINT,

    -- Audit columns present on every table
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- Soft-delete flag; queries must always filter WHERE is_deleted = false
    is_deleted  BOOLEAN         NOT NULL DEFAULT FALSE,

    -- Foreign key to users table
    -- NEVER use ON DELETE CASCADE per CLAUDE.md §6
    -- NEVER use ON DELETE SET NULL per CLAUDE.md §6
    -- If a user is deleted, their categories remain in the database with orphaned user_id
    -- (In practice, user deletes are soft deletes, so this constraint is always valid)
    CONSTRAINT fk_categories_users FOREIGN KEY (user_id) REFERENCES users(id),

    -- Unique constraint: category_key must be unique within a user's scope
    -- For system categories (user_id IS NULL), category_key must be globally unique
    -- For user-defined categories, category_key must be unique per user
    -- PostgreSQL treats NULL as distinct, so multiple system categories with the same key are allowed
    -- unless we use a partial unique index (see below)

    CONSTRAINT uq_categories_user_category_key UNIQUE (user_id, category_key)
);

-- Unique index for system categories (user_id IS NULL, category_key)
-- Ensures system category keys are globally unique
-- This is a partial unique index, only applies when user_id IS NULL
CREATE UNIQUE INDEX IF NOT EXISTS uq_categories_system_category_key
    ON categories (category_key)
    WHERE user_id IS NULL;

-- ============================================================
-- Rollback script (run manually if migration must be reverted)
-- DROP TABLE IF EXISTS categories;
-- ============================================================
