-- Add notification columns to action_items with defaults for existing rows
ALTER TABLE action_items
    ADD COLUMN IF NOT EXISTS notify_on_scheduled_time BOOLEAN DEFAULT FALSE;

UPDATE action_items SET notify_on_scheduled_time = FALSE WHERE notify_on_scheduled_time IS NULL;

ALTER TABLE action_items
    ALTER COLUMN notify_on_scheduled_time SET NOT NULL;

ALTER TABLE action_items
    ADD COLUMN IF NOT EXISTS notify_minutes_before INTEGER;

ALTER TABLE action_items
    ADD COLUMN IF NOT EXISTS target_per_completion NUMERIC(19, 2);

ALTER TABLE action_items
    ADD COLUMN IF NOT EXISTS target_unit VARCHAR(50);

-- Add week_start_day to user_preferences
ALTER TABLE user_preferences
    ADD COLUMN IF NOT EXISTS week_start_day INTEGER DEFAULT 1;

-- Create task_schedule_slots table
CREATE TABLE IF NOT EXISTS task_schedule_slots (
    id BIGSERIAL PRIMARY KEY,
    action_item_id BIGINT NOT NULL REFERENCES action_items(id),
    slot_index INTEGER NOT NULL,
    specific_time VARCHAR(5),
    created_via VARCHAR(20) NOT NULL,
    effective_from DATE NOT NULL,
    effective_until DATE,
    rescheduled_from_slot_id BIGINT REFERENCES task_schedule_slots(id),
    reschedule_reason VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_task_schedule_slots_action_item ON task_schedule_slots(action_item_id);
CREATE INDEX IF NOT EXISTS idx_task_schedule_slots_effective ON task_schedule_slots(effective_from, effective_until);

-- Add new columns to task_completions
ALTER TABLE task_completions
    ADD COLUMN IF NOT EXISTS schedule_slot_id BIGINT REFERENCES task_schedule_slots(id);

ALTER TABLE task_completions
    ADD COLUMN IF NOT EXISTS period_start DATE;

ALTER TABLE task_completions
    ADD COLUMN IF NOT EXISTS scheduled_date DATE;

ALTER TABLE task_completions
    ADD COLUMN IF NOT EXISTS scheduled_time VARCHAR(5);

ALTER TABLE task_completions
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'COMPLETED';

-- Update existing completions to have scheduled_date from date column if it exists
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'task_completions' AND column_name = 'date') THEN
        UPDATE task_completions SET scheduled_date = date WHERE scheduled_date IS NULL AND date IS NOT NULL;
    END IF;
END $$;

-- Set defaults for existing rows
UPDATE task_completions SET scheduled_date = CURRENT_DATE WHERE scheduled_date IS NULL;
UPDATE task_completions SET period_start = date_trunc('week', scheduled_date)::date WHERE period_start IS NULL;
UPDATE task_completions SET status = 'COMPLETED' WHERE status IS NULL;

ALTER TABLE task_completions
    ALTER COLUMN scheduled_date SET NOT NULL;

ALTER TABLE task_completions
    ALTER COLUMN period_start SET NOT NULL;

ALTER TABLE task_completions
    ALTER COLUMN status SET NOT NULL;

-- Create streak_info table
CREATE TABLE IF NOT EXISTS streak_info (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    goal_id BIGINT REFERENCES goals(id),
    action_item_id BIGINT REFERENCES action_items(id),
    current_maintained_streak INTEGER NOT NULL DEFAULT 0,
    best_maintained_streak INTEGER NOT NULL DEFAULT 0,
    current_perfect_streak INTEGER NOT NULL DEFAULT 0,
    best_perfect_streak INTEGER NOT NULL DEFAULT 0,
    last_updated_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    UNIQUE(user_id, goal_id, action_item_id)
);

CREATE INDEX IF NOT EXISTS idx_streak_info_user ON streak_info(user_id);
CREATE INDEX IF NOT EXISTS idx_streak_info_goal ON streak_info(goal_id);
CREATE INDEX IF NOT EXISTS idx_streak_info_action_item ON streak_info(action_item_id);
