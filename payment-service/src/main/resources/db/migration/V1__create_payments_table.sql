CREATE TABLE payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id UUID,
    user_id VARCHAR(255),
    status VARCHAR(50),
    created_at TIMESTAMP,
    version INTEGER NOT NULL DEFAULT 0,
    reason TEXT
);

CREATE UNIQUE INDEX idx_payments_order_id ON payments (order_id);
