-- Add soft delete and premium tracking columns to goals table
-- This migration supports the dynamic goal lock feature

-- Add deleted_at column for soft delete functionality
ALTER TABLE goals ADD COLUMN deleted_at DATE;

-- Add created_during_premium flag to track if goal was created while user had premium subscription
ALTER TABLE goals ADD COLUMN created_during_premium BOOLEAN NOT NULL DEFAULT FALSE;

-- Add previous_status to remember status before locking (for restoration when unlocking)
ALTER TABLE goals ADD COLUMN previous_status VARCHAR(50);

-- Create index for efficient soft delete queries
CREATE INDEX idx_goals_deleted_at ON goals(deleted_at);

-- Create index for finding premium goals for locking
CREATE INDEX idx_goals_premium_locking ON goals(user_id, created_during_premium, deleted_at, archived_at, goal_status);

-- Create index for finding locked goals for unlocking
CREATE INDEX idx_goals_locked ON goals(user_id, goal_status, deleted_at) WHERE goal_status = 'LOCKED';
