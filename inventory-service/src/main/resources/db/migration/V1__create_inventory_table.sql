CREATE TABLE inventory (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id VARCHAR(255),
    available_quantity INT,
    version BIGINT
);