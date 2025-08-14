CREATE TABLE orders (
    id              UUID PRIMARY KEY,
    user_id         VARCHAR(255)  NOT NULL,
    product_id      VARCHAR(255)  NOT NULL,
    status          VARCHAR(50)   NOT NULL,
    created_at      TIMESTAMP     NOT NULL,
    updated_at      TIMESTAMP     NOT NULL,
    failure_reason  VARCHAR(255),
    version         INT           NOT NULL DEFAULT 0
);

CREATE INDEX idx_orders_status_updated_at ON orders (status, updated_at);