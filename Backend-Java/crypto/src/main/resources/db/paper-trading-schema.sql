-- Paper-trading tables (MySQL). Created idempotently on startup by spring.sql.init
-- (continue-on-error=true). Hibernate ddl-auto stays 'none' — this file owns the schema,
-- mirroring how the BATCH_* tables are created. Non-destructive: only CREATE TABLE IF NOT EXISTS.

CREATE TABLE IF NOT EXISTS paper_candles (
    candle_id    BIGINT        NOT NULL AUTO_INCREMENT,
    timeframe    VARCHAR(20)   NOT NULL,
    candle_time  DATETIME      NOT NULL,
    open_price   DECIMAL(20,8),
    high_price   DECIMAL(20,8),
    low_price    DECIMAL(20,8),
    close_price  DECIMAL(20,8),
    volume       DECIMAL(30,8),
    created_at   DATETIME,
    updated_at   DATETIME,
    PRIMARY KEY (candle_id),
    UNIQUE KEY uk_paper_candle_tf_time (timeframe, candle_time),
    KEY idx_paper_candle_time (candle_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS paper_trades (
    trade_id           BIGINT        NOT NULL AUTO_INCREMENT,
    timeframe          VARCHAR(20)   NOT NULL,
    trade_type         VARCHAR(16)   NOT NULL,
    trade_status       VARCHAR(16)   NOT NULL,
    pattern_name       VARCHAR(48)   NOT NULL,
    candle_time        DATETIME      NOT NULL,
    candle_id          BIGINT        NOT NULL,
    trade_price        DECIMAL(20,8),
    stop_loss          DECIMAL(20,8),
    initial_stop_loss  DECIMAL(20,8),
    tp1                DECIMAL(20,8),
    tp2                DECIMAL(20,8),
    tp3                DECIMAL(20,8),
    tp4                DECIMAL(20,8),
    safe_trade         BOOLEAN       NOT NULL DEFAULT 0,
    tp1_achieved       BOOLEAN       NOT NULL DEFAULT 0,
    tp2_achieved       BOOLEAN       NOT NULL DEFAULT 0,
    tp3_achieved       BOOLEAN       NOT NULL DEFAULT 0,
    tp4_achieved       BOOLEAN       NOT NULL DEFAULT 0,
    close_price        DECIMAL(20,8),
    close_reason       VARCHAR(40),
    opened_at          DATETIME,
    closed_at          DATETIME,
    last_evaluated_at  DATETIME,
    confidence_score   DECIMAL(6,4),
    detection_reason   VARCHAR(500),
    pattern_start_time DATETIME,
    pattern_end_time   DATETIME,
    breakout_level     DECIMAL(20,8),
    risk_amount        DECIMAL(20,8),
    atr_at_detection   DECIMAL(20,8),
    one_minute_crossover_state     VARCHAR(16),
    five_minute_crossover_state    VARCHAR(16),
    fifteen_minute_crossover_state VARCHAR(16),
    one_hour_crossover_state       VARCHAR(16),
    created_by_version VARCHAR(32),
    created_at         DATETIME,
    updated_at         DATETIME,
    version            BIGINT,
    PRIMARY KEY (trade_id),
    UNIQUE KEY uk_paper_trade_dedup (timeframe, pattern_name, trade_type, candle_time),
    KEY idx_paper_trade_status (trade_status),
    KEY idx_paper_trade_timeframe (timeframe),
    KEY idx_paper_trade_pattern (pattern_name),
    KEY idx_paper_trade_type (trade_type),
    KEY idx_paper_trade_candle_time (candle_time),
    KEY idx_paper_trade_created_at (created_at),
    CONSTRAINT fk_paper_trade_candle FOREIGN KEY (candle_id) REFERENCES paper_candles (candle_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Widen close_reason for DISCARDED recovery reasons on DBs created before 2026-06-06
-- (idempotent; no-op when already 40; continue-on-error tolerates any failure).
ALTER TABLE paper_trades MODIFY close_reason VARCHAR(40);
