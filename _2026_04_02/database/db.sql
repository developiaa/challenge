CREATE TABLE IF NOT EXISTS card_issues
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT      NOT NULL,
    status     VARCHAR(30) NOT NULL,
    created_at DATETIME(6) NOT NULL
)
