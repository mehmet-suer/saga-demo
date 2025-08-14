CREATE TABLE processed_events (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_id UUID NOT NULL,
    event_key UUID,
    event_type VARCHAR(100) NOT NULL,
    processed_at TIMESTAMP,
    topic VARCHAR(255),
    version INTEGER NOT NULL DEFAULT 0,
    payload CLOB,
    headers_payload CLOB,
    sent BOOLEAN NOT NULL DEFAULT FALSE,
    retry_count INTEGER NOT NULL DEFAULT 0,
    last_tried_at TIMESTAMP
);

CREATE UNIQUE INDEX idx_processed_event_id_type ON processed_events (event_id, event_type);
CREATE INDEX idx_processed_event_sent_processed_at ON processed_events (sent, processed_at);