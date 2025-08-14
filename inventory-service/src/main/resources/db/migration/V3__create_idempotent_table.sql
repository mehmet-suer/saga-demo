CREATE TABLE idempotent_events (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    processed_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE UNIQUE INDEX idx_event_id_type ON idempotent_events (event_id, event_type);
