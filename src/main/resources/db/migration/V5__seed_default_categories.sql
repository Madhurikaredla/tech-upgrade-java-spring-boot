-- ============================================================
-- Migration : V5__seed_default_categories.sql
-- Purpose   : Seeds default system-level categories available to all users.
--             System categories have user_id = NULL.
--             Users can create their own categories in addition to these.
-- ============================================================

-- Insert default system categories
-- These categories are available to all users and cannot be modified or deleted by users
INSERT INTO categories (name, category_key, user_id, created_at, updated_at, is_deleted)
VALUES
    ('Food', 'FOOD', NULL, NOW(), NOW(), false),
    ('Transport', 'TRANSPORT', NULL, NOW(), NOW(), false),
    ('Entertainment', 'ENTERTAINMENT', NULL, NOW(), NOW(), false),
    ('Shopping', 'SHOPPING', NULL, NOW(), NOW(), false),
    ('Healthcare', 'HEALTHCARE', NULL, NOW(), NOW(), false),
    ('Education', 'EDUCATION', NULL, NOW(), NOW(), false),
    ('Utilities', 'UTILITIES', NULL, NOW(), NOW(), false),
    ('Rent', 'RENT', NULL, NOW(), NOW(), false),
    ('Groceries', 'GROCERIES', NULL, NOW(), NOW(), false),
    ('Travel', 'TRAVEL', NULL, NOW(), NOW(), false),
    ('Personal Care', 'PERSONAL_CARE', NULL, NOW(), NOW(), false),
    ('Insurance', 'INSURANCE', NULL, NOW(), NOW(), false),
    ('Others', 'OTHERS', NULL, NOW(), NOW(), false)
ON CONFLICT (user_id, category_key) DO NOTHING;

-- ============================================================
-- Rollback script (run manually if migration must be reverted)
-- DELETE FROM categories WHERE user_id IS NULL;
-- ============================================================
