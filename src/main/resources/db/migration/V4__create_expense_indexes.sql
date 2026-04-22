-- =============================================================
-- V4__create_expense_indexes.sql
-- Creates indexes for the expenses and expense_category_map tables.
--
-- Rules (CLAUDE.md §6):
--   - Every FK column must be indexed
--   - Indexes defined in a dedicated migration, never inline in CREATE TABLE
--   - Index name format: idx_{table}_{column(s)}
-- =============================================================

-- expenses: index on user_id (FK + frequent filter column)
CREATE INDEX IF NOT EXISTS idx_expenses_user_id
    ON expenses (user_id);

-- expenses: composite index on (user_id, expense_date DESC) for frequent queries
CREATE INDEX IF NOT EXISTS idx_expenses_user_id_expense_date
    ON expenses (user_id, expense_date DESC);

-- expenses: index on is_deleted for soft-delete filtering
CREATE INDEX IF NOT EXISTS idx_expenses_is_deleted
    ON expenses (is_deleted);

-- expense_category_map: index on expense_id (FK)
CREATE INDEX IF NOT EXISTS idx_expense_category_map_expense_id
    ON expense_category_map (expense_id);

-- expense_category_map: index on category_id (FK)
CREATE INDEX IF NOT EXISTS idx_expense_category_map_category_id
    ON expense_category_map (category_id);

-- expense_category_map: index on is_deleted for soft-delete filtering
CREATE INDEX IF NOT EXISTS idx_expense_category_map_is_deleted
    ON expense_category_map (is_deleted);

-- =============================================================
-- Rollback:
--   DROP INDEX IF EXISTS idx_expense_category_map_is_deleted;
--   DROP INDEX IF EXISTS idx_expense_category_map_category_id;
--   DROP INDEX IF EXISTS idx_expense_category_map_expense_id;
--   DROP INDEX IF EXISTS idx_expenses_is_deleted;
--   DROP INDEX IF EXISTS idx_expenses_user_id_expense_date;
--   DROP INDEX IF EXISTS idx_expenses_user_id;
-- =============================================================
