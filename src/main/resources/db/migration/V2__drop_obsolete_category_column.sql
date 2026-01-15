-- Drop obsolete 'category' column from goals table
-- This column was created by Hibernate's ddl-auto before migrations were set up
-- The correct column is 'goal_category'

ALTER TABLE goals DROP COLUMN IF EXISTS category;
