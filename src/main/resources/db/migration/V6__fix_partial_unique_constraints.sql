-- =============================================================
-- V6__fix_partial_unique_constraints.sql
-- Replaces full unique constraints on expense_category_map and
-- categories with partial unique indexes that only enforce
-- uniqueness among non-deleted rows (is_deleted = false).
--
-- Motivation:
--   The original full unique constraints include soft-deleted rows,
--   which means:
--     - Re-adding a category mapping to an updated expense fails
--       with a duplicate key error on uq_expense_category.
--     - Reusing a category_key after soft-deleting a category fails
--       on uq_categories_user_category_key / category_key.
--   Partial indexes solve both issues without any data loss.
-- =============================================================

-- ── expense_category_map ──────────────────────────────────────

-- Drop the full unique constraint
ALTER TABLE expense_category_map
    DROP CONSTRAINT IF EXISTS uq_expense_category;

-- Create partial unique index: unique only among active (non-deleted) rows
CREATE UNIQUE INDEX IF NOT EXISTS uq_expense_category
    ON expense_category_map (expense_id, category_id)
    WHERE is_deleted = false;

-- ── categories ────────────────────────────────────────────────

-- Drop the full inline unique constraint on category_key (created by UNIQUE keyword in V2)
-- PostgreSQL auto-names it categories_category_key_key
ALTER TABLE categories
    DROP CONSTRAINT IF EXISTS categories_category_key_key;

-- Drop the composite unique constraint on (user_id, category_key)
ALTER TABLE categories
    DROP CONSTRAINT IF EXISTS uq_categories_user_category_key;

-- Drop the partial unique index for system categories (will be recreated below)
DROP INDEX IF EXISTS uq_categories_system_category_key;

-- Create partial unique index for (user_id, category_key) among non-deleted rows
CREATE UNIQUE INDEX IF NOT EXISTS uq_categories_user_category_key
    ON categories (user_id, category_key)
    WHERE is_deleted = false;

-- Create partial unique index for system category keys (user_id IS NULL) among non-deleted rows
CREATE UNIQUE INDEX IF NOT EXISTS uq_categories_system_category_key
    ON categories (category_key)
    WHERE user_id IS NULL AND is_deleted = false;

-- =============================================================
-- Rollback:
--   DROP INDEX IF EXISTS uq_expense_category;
--   ALTER TABLE expense_category_map
--       ADD CONSTRAINT uq_expense_category UNIQUE (expense_id, category_id);
--
--   DROP INDEX IF EXISTS uq_categories_user_category_key;
--   DROP INDEX IF EXISTS uq_categories_system_category_key;
--   ALTER TABLE categories
--       ADD CONSTRAINT uq_categories_user_category_key UNIQUE (user_id, category_key);
--   ALTER TABLE categories
--       ADD CONSTRAINT categories_category_key_key UNIQUE (category_key);
-- =============================================================
