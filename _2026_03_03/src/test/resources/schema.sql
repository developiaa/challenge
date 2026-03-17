CREATE TABLE orders
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT      NOT NULL,
    product_id BIGINT      NOT NULL,
    status     VARCHAR(50) NOT NULL,
    created_at TIMESTAMP   NOT NULL
);
