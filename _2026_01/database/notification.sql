CREATE TABLE notifications
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    requester_id       VARCHAR(255) NOT NULL,
    idempotency_key    VARCHAR(255) NOT NULL,
    channel            VARCHAR(20)  NOT NULL COMMENT 'EMAIL, SMS, KAKAOTALK',
    title              VARCHAR(255) NOT NULL,
    content            TEXT,
    target_destination VARCHAR(255) NOT NULL COMMENT '이메일, 전화번호, 톡ID',
    status             VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, SENT, FAILED, CANCELED',
    scheduled_at       DATETIME NULL COMMENT '고객 요청 발송 시각 (History용)',
    target_at          DATETIME     NOT NULL COMMENT '시스템 실행 예정 시각 (Scheduler용)',
    sent_at            DATETIME,
    retry_count        INT                   DEFAULT 0,
    created_at         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_idempotency_key UNIQUE (idempotency_key)
);


-- 커서 기반 페이징용
CREATE INDEX idx_requester_cursor ON notifications (requester_id, id DESC);

-- 스케줄러
CREATE INDEX idx_status_scheduled ON notifications (status, target_at);

-- 이력 조회용
CREATE INDEX idx_created_at ON notifications (created_at);
