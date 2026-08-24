ALTER TABLE performance_reviews
    ADD COLUMN IF NOT EXISTS manager_approved_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS head_approved_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS rejected_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS rejection_comments TEXT;
