-- Metasmart Initial Schema
-- Version 1.0.0

-- Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    streak_shields INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE INDEX idx_users_email ON users(email);

-- User Preferences table
CREATE TABLE user_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id),
    timezone VARCHAR(100) NOT NULL DEFAULT 'UTC',
    language VARCHAR(10) NOT NULL DEFAULT 'en',
    email_notifications BOOLEAN NOT NULL DEFAULT TRUE,
    push_notifications BOOLEAN NOT NULL DEFAULT TRUE,
    weekly_digest BOOLEAN NOT NULL DEFAULT TRUE,
    streak_reminders BOOLEAN NOT NULL DEFAULT TRUE,
    guardian_nudges BOOLEAN NOT NULL DEFAULT TRUE,
    preferred_reminder_time VARCHAR(10),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

-- Notification Preferences table
CREATE TABLE notification_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id),
    push_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    push_goal_reminders BOOLEAN NOT NULL DEFAULT TRUE,
    push_progress_reminders BOOLEAN NOT NULL DEFAULT TRUE,
    push_milestones BOOLEAN NOT NULL DEFAULT TRUE,
    push_streak_alerts BOOLEAN NOT NULL DEFAULT TRUE,
    push_guardian_nudges BOOLEAN NOT NULL DEFAULT TRUE,
    email_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    email_weekly_digest BOOLEAN NOT NULL DEFAULT TRUE,
    email_milestones BOOLEAN NOT NULL DEFAULT TRUE,
    email_streak_at_risk BOOLEAN NOT NULL DEFAULT TRUE,
    whatsapp_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    whatsapp_number VARCHAR(20),
    quiet_hours_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    quiet_hours_start VARCHAR(10),
    quiet_hours_end VARCHAR(10),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

-- Password Reset Tokens table
CREATE TABLE password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL REFERENCES users(id),
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_password_reset_tokens_token ON password_reset_tokens(token);
CREATE INDEX idx_password_reset_tokens_user ON password_reset_tokens(user_id);

-- Goals table
CREATE TABLE goals (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    title VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    goal_category VARCHAR(50) NOT NULL,
    target_value VARCHAR(255) NOT NULL,
    unit VARCHAR(255) NOT NULL,
    current_progress DECIMAL(19, 2) NOT NULL DEFAULT 0,
    motivation VARCHAR(500),
    start_date DATE NOT NULL,
    target_date DATE NOT NULL,
    goal_status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    archived_at DATE,
    last_streak_shield_used_at DATE,
    streak INTEGER DEFAULT 0,
    tags VARCHAR(500),
    action_plan_overview VARCHAR(2000),
    -- Embedded GoalPillars
    pillar_clarity VARCHAR(1000),
    pillar_metric VARCHAR(500),
    pillar_action_plan VARCHAR(2000),
    pillar_deadline VARCHAR(500),
    pillar_motivation VARCHAR(1000),
    -- Embedded GoalMeasurement
    measurement_type VARCHAR(50),
    measurement_track_frequency VARCHAR(50),
    measurement_baseline VARCHAR(100),
    -- Embedded GoalReminders
    reminder_enabled BOOLEAN DEFAULT TRUE,
    reminder_frequency VARCHAR(50),
    reminder_time VARCHAR(10),
    reminder_channels VARCHAR(255),
    -- Embedded EmotionalAnchors
    emotional_vision VARCHAR(1000),
    emotional_celebration VARCHAR(500),
    emotional_affirmation VARCHAR(500),
    -- Embedded AiSupport
    ai_suggestions_enabled BOOLEAN DEFAULT TRUE,
    ai_auto_adjustments BOOLEAN DEFAULT FALSE,
    ai_motivation_style VARCHAR(50),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE INDEX idx_goals_user ON goals(user_id);
CREATE INDEX idx_goals_status ON goals(goal_status);
CREATE INDEX idx_goals_user_status ON goals(user_id, goal_status);

-- Goal Notes table
CREATE TABLE goal_notes (
    id BIGSERIAL PRIMARY KEY,
    goal_id BIGINT NOT NULL REFERENCES goals(id) ON DELETE CASCADE,
    content VARCHAR(2000) NOT NULL,
    note_type VARCHAR(50) NOT NULL DEFAULT 'GENERAL',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE INDEX idx_goal_notes_goal ON goal_notes(goal_id);

-- Progress Entries table
CREATE TABLE progress_entries (
    id BIGSERIAL PRIMARY KEY,
    goal_id BIGINT NOT NULL REFERENCES goals(id) ON DELETE CASCADE,
    progress_value DECIMAL(10, 2) NOT NULL,
    note VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE INDEX idx_progress_entries_goal ON progress_entries(goal_id);
CREATE INDEX idx_progress_entries_created ON progress_entries(created_at);

-- Milestones table
CREATE TABLE milestones (
    id BIGSERIAL PRIMARY KEY,
    goal_id BIGINT NOT NULL REFERENCES goals(id) ON DELETE CASCADE,
    percentage INTEGER NOT NULL,
    description VARCHAR(255),
    achieved_at TIMESTAMP,
    achieved BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE INDEX idx_milestones_goal ON milestones(goal_id);

-- Obstacle Entries table
CREATE TABLE obstacle_entries (
    id BIGSERIAL PRIMARY KEY,
    goal_id BIGINT NOT NULL REFERENCES goals(id) ON DELETE CASCADE,
    entry_date DATE NOT NULL,
    obstacle VARCHAR(1000) NOT NULL,
    solution VARCHAR(1000),
    resolved BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE INDEX idx_obstacle_entries_goal ON obstacle_entries(goal_id);

-- Goal Templates table
CREATE TABLE goal_templates (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    default_title VARCHAR(255),
    default_description VARCHAR(1000),
    default_category VARCHAR(50),
    default_target_value VARCHAR(50),
    default_unit VARCHAR(50),
    default_motivation VARCHAR(500),
    default_duration_days INTEGER NOT NULL DEFAULT 90,
    is_public BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE INDEX idx_goal_templates_user ON goal_templates(user_id);
CREATE INDEX idx_goal_templates_public ON goal_templates(is_public);

-- Goal Guardians table
CREATE TABLE goal_guardians (
    id BIGSERIAL PRIMARY KEY,
    goal_id BIGINT NOT NULL REFERENCES goals(id) ON DELETE CASCADE,
    guardian_id BIGINT NOT NULL REFERENCES users(id),
    owner_id BIGINT NOT NULL REFERENCES users(id),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    invite_message VARCHAR(500),
    accepted_at TIMESTAMP,
    declined_at TIMESTAMP,
    revoked_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    UNIQUE(goal_id, guardian_id)
);

CREATE INDEX idx_goal_guardians_goal ON goal_guardians(goal_id);
CREATE INDEX idx_goal_guardians_guardian ON goal_guardians(guardian_id);
CREATE INDEX idx_goal_guardians_owner ON goal_guardians(owner_id);

-- Guardian Permissions table (ElementCollection)
CREATE TABLE guardian_permissions (
    goal_guardian_id BIGINT NOT NULL REFERENCES goal_guardians(id) ON DELETE CASCADE,
    permission VARCHAR(50) NOT NULL,
    PRIMARY KEY (goal_guardian_id, permission)
);

-- Guardian Nudges table
CREATE TABLE guardian_nudges (
    id BIGSERIAL PRIMARY KEY,
    goal_guardian_id BIGINT NOT NULL REFERENCES goal_guardians(id) ON DELETE CASCADE,
    message VARCHAR(500) NOT NULL,
    nudge_type VARCHAR(50) NOT NULL,
    read_at TIMESTAMP,
    reaction VARCHAR(10),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE INDEX idx_guardian_nudges_guardian ON guardian_nudges(goal_guardian_id);

-- Goal Reflections table
CREATE TABLE goal_reflections (
    id BIGSERIAL PRIMARY KEY,
    goal_id BIGINT NOT NULL REFERENCES goals(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id),
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    rating VARCHAR(50) NOT NULL,
    went_well VARCHAR(1000),
    challenges VARCHAR(1000),
    adjustments VARCHAR(1000),
    mood_note VARCHAR(500),
    will_continue BOOLEAN,
    motivation_level INTEGER,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE INDEX idx_goal_reflections_goal ON goal_reflections(goal_id);
CREATE INDEX idx_goal_reflections_user ON goal_reflections(user_id);

-- Struggling Requests table
CREATE TABLE struggling_requests (
    id BIGSERIAL PRIMARY KEY,
    goal_id BIGINT NOT NULL REFERENCES goals(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id),
    struggling_type VARCHAR(50) NOT NULL,
    user_message VARCHAR(1000),
    ai_suggestion VARCHAR(2000),
    notify_guardians BOOLEAN,
    guardians_notified BOOLEAN,
    was_helpful BOOLEAN,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE INDEX idx_struggling_requests_goal ON struggling_requests(goal_id);
CREATE INDEX idx_struggling_requests_user ON struggling_requests(user_id);

-- Action Items table
CREATE TABLE action_items (
    id BIGSERIAL PRIMARY KEY,
    goal_id BIGINT NOT NULL REFERENCES goals(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    task_type VARCHAR(50) NOT NULL DEFAULT 'ONE_TIME',
    target_date DATE,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at TIMESTAMP,
    priority VARCHAR(50) NOT NULL DEFAULT 'MEDIUM',
    order_index INTEGER,
    impact_score INTEGER DEFAULT 5,
    effort_estimate INTEGER DEFAULT 5,
    context VARCHAR(500),
    dependencies VARCHAR(500),
    notes VARCHAR(1000),
    -- Embedded TaskRecurrence
    recurrence_enabled BOOLEAN,
    recurrence_frequency VARCHAR(50),
    recurrence_interval INTEGER,
    recurrence_days_of_week VARCHAR(50),
    recurrence_ends_at DATE,
    -- Embedded FrequencyGoal
    frequency_goal_target INTEGER,
    frequency_goal_period VARCHAR(50),
    frequency_goal_current INTEGER,
    -- Embedded ReminderOverride
    reminder_override_enabled BOOLEAN,
    reminder_override_time VARCHAR(10),
    reminder_override_days_before INTEGER,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE INDEX idx_action_items_goal ON action_items(goal_id);
CREATE INDEX idx_action_items_completed ON action_items(completed);

-- Scheduled Tasks table
CREATE TABLE scheduled_tasks (
    id BIGSERIAL PRIMARY KEY,
    action_item_id BIGINT NOT NULL REFERENCES action_items(id) ON DELETE CASCADE,
    scheduled_date DATE NOT NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE INDEX idx_scheduled_tasks_action_item ON scheduled_tasks(action_item_id);
CREATE INDEX idx_scheduled_tasks_date ON scheduled_tasks(scheduled_date);

-- Task Completions table
CREATE TABLE task_completions (
    id BIGSERIAL PRIMARY KEY,
    action_item_id BIGINT NOT NULL REFERENCES action_items(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    completed_at TIMESTAMP NOT NULL,
    note VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE INDEX idx_task_completions_action_item ON task_completions(action_item_id);
CREATE INDEX idx_task_completions_date ON task_completions(date);

-- User Subscriptions table
CREATE TABLE user_subscriptions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    tier VARCHAR(50) NOT NULL DEFAULT 'FREE',
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    trial_end_date TIMESTAMP,
    cancelled_at TIMESTAMP,
    external_subscription_id VARCHAR(255),
    payment_provider VARCHAR(50),
    price_amount DECIMAL(10, 2),
    price_currency VARCHAR(10),
    billing_period VARCHAR(50),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE INDEX idx_user_subscriptions_user ON user_subscriptions(user_id);
CREATE INDEX idx_user_subscriptions_status ON user_subscriptions(status);
CREATE INDEX idx_user_subscriptions_external ON user_subscriptions(external_subscription_id);

-- User Purchases table
CREATE TABLE user_purchases (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    purchase_type VARCHAR(50) NOT NULL,
    quantity INTEGER DEFAULT 1,
    quantity_remaining INTEGER DEFAULT 1,
    price_amount DECIMAL(10, 2),
    price_currency VARCHAR(10),
    external_transaction_id VARCHAR(255),
    payment_provider VARCHAR(50),
    purchased_at TIMESTAMP,
    expires_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE INDEX idx_user_purchases_user ON user_purchases(user_id);
CREATE INDEX idx_user_purchases_type ON user_purchases(purchase_type);
CREATE INDEX idx_user_purchases_external ON user_purchases(external_transaction_id);
