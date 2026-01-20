-- Drop the legacy 'date' column from task_completions
-- This column was replaced by 'scheduled_date' in V5

-- First, copy any remaining data from 'date' to 'scheduled_date' if not already done
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'task_completions' AND column_name = 'date') THEN
        UPDATE task_completions SET scheduled_date = date WHERE scheduled_date IS NULL AND date IS NOT NULL;

        -- Now drop the column
        ALTER TABLE task_completions DROP COLUMN IF EXISTS date;
    END IF;
END $$;
