CREATE TABLE products
(
    product_id       varchar(255) PRIMARY KEY,
    seller_id        BIGINT       NOT NULL,
    category         VARCHAR(255) NOT NULL,
    product_name     VARCHAR(255) NOT NULL,
    sales_start_date DATE,
    sales_end_date   DATE,
    product_status   VARCHAR(50),
    brand            VARCHAR(255),
    manufacturer     VARCHAR(255),
    sales_price      INTEGER      NOT NULL,
    stock_quantity   INTEGER   DEFAULT 0,
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_products_status ON products (product_status);
CREATE INDEX idx_products_category ON products (category);
CREATE INDEX idx_products_brand ON products (brand);
CREATE INDEX idx_products_manufacturer ON products (manufacturer);
CREATE INDEX idx_products_seller_id ON products (seller_id);

-- CDC용 계정 생성
CREATE USER 'debezium'@'%' IDENTIFIED BY 'dbz';
GRANT SELECT, RELOAD, SHOW DATABASES, REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'debezium'@'%';
FLUSH PRIVILEGES;

-- 2026_03 모듈에서 사용
-- 유저 테이블
CREATE TABLE users
(
    user_id  BIGINT NOT NULL,
    username VARCHAR(255),
    PRIMARY KEY (user_id)
);

-- 게시글 테이블 (user_id 추가)
CREATE TABLE articles
(
    article_id BIGINT NOT NULL,
    user_id    BIGINT NOT NULL, -- 샤딩 키로 사용될 외래키 성격의 컬럼
    title      VARCHAR(255),
    content    TEXT,
    PRIMARY KEY (article_id)
);

-- 댓글 테이블 (user_id 추가)
CREATE TABLE comments
(
    comment_id BIGINT NOT NULL,
    article_id BIGINT,
    user_id    BIGINT NOT NULL, -- 샤딩 키로 사용될 외래키 성격의 컬럼
    content    VARCHAR(255),
    PRIMARY KEY (comment_id)
);
