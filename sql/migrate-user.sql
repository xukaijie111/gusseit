-- 用户表 + 答题历史表
-- 用法: mysql -uroot -p guseeit < sql/migrate-user.sql

CREATE TABLE IF NOT EXISTS users (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    openid      VARCHAR(128) NOT NULL UNIQUE,
    phone       VARCHAR(20)  NULL,
    session_key VARCHAR(128) NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_openid (openid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_histories (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT       NOT NULL,
    round_id        VARCHAR(64)  NOT NULL,
    guess_city      VARCHAR(64)  NULL,
    guess_lat       DOUBLE       NULL,
    guess_lng       DOUBLE       NULL,
    guess_year      INT          NULL,
    guess_dynasty    VARCHAR(32)  NULL,
    answer_city     VARCHAR(64)  NULL,
    answer_lat      DOUBLE       NULL,
    answer_lng      DOUBLE       NULL,
    answer_year     INT          NULL,
    answer_dynasty   VARCHAR(32)  NULL,
    total_score     INT          NOT NULL DEFAULT 0,
    dynasty_score   INT          NOT NULL DEFAULT 0,
    geo_score       INT          NOT NULL DEFAULT 0,
    distance_km     DOUBLE       NOT NULL DEFAULT 0,
    image_url       VARCHAR(512) NULL,
    location_name   VARCHAR(128) NULL,
    modern_place    VARCHAR(64)  NULL,
    answered_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_user_round (user_id, round_id),
    CONSTRAINT fk_history_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
