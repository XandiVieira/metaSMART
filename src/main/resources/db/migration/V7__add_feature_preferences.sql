-- Feature preferences table for user-configurable feature toggles
CREATE TABLE feature_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    daily_journal_enabled BOOLEAN NOT NULL DEFAULT true,
    streaks_enabled BOOLEAN NOT NULL DEFAULT true,
    achievements_enabled BOOLEAN NOT NULL DEFAULT true,
    analytics_enabled BOOLEAN NOT NULL DEFAULT true,
    flight_plan_enabled BOOLEAN NOT NULL DEFAULT true,
    progress_reminders_enabled BOOLEAN NOT NULL DEFAULT true,
    milestones_enabled BOOLEAN NOT NULL DEFAULT true,
    obstacle_tracking_enabled BOOLEAN NOT NULL DEFAULT true,
    reflections_enabled BOOLEAN NOT NULL DEFAULT true,
    social_proof_enabled BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE INDEX idx_feature_preferences_user_id ON feature_preferences(user_id);
