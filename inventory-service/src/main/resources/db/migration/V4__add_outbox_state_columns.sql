ALTER TABLE processed_events
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS status VARCHAR(20),
    ADD COLUMN IF NOT EXISTS locked_until TIMESTAMP,
    ADD COLUMN IF NOT EXISTS next_retry_at TIMESTAMP;

UPDATE processed_events
SET created_at = COALESCE(created_at, processed_at, now())
WHERE created_at IS NULL;

UPDATE processed_events
SET status = CASE
                 WHEN sent THEN 'SENT'
                 ELSE 'NEW'
             END
WHERE status IS NULL;

CREATE INDEX IF NOT EXISTS idx_processed_events_new_created_at
    ON processed_events (created_at)
    WHERE status = 'NEW';

CREATE INDEX IF NOT EXISTS idx_processed_events_in_progress_lock_created_at
    ON processed_events (locked_until, created_at)
    WHERE status = 'IN_PROGRESS';

CREATE INDEX IF NOT EXISTS idx_processed_events_retry_next_created_at
    ON processed_events (next_retry_at, created_at)
    WHERE status = 'RETRY';
