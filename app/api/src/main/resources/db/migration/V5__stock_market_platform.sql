-- Phase 3.5: Stock market data platform schema (mirrors the Phase 3 mutual fund platform pattern)

CREATE TABLE stock_exchanges (
    id          UUID PRIMARY KEY,
    code        VARCHAR(20)  NOT NULL,
    name        VARCHAR(100) NOT NULL,
    country     VARCHAR(50)  NOT NULL DEFAULT 'IN',
    timezone    VARCHAR(50)  NOT NULL DEFAULT 'Asia/Kolkata',
    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_stock_exchanges_code UNIQUE (code)
);

CREATE TABLE stocks (
    id              UUID PRIMARY KEY,
    symbol          VARCHAR(50)   NOT NULL,
    exchange_id     UUID          NOT NULL,
    instrument_key  VARCHAR(100)  NOT NULL,
    isin            VARCHAR(20),
    company_name    VARCHAR(300)  NOT NULL,
    sector          VARCHAR(150),
    series          VARCHAR(20),
    lot_size        INTEGER,
    tick_size       NUMERIC(10, 4),
    status          VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
    last_synced_at  TIMESTAMPTZ,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT fk_stocks_exchange FOREIGN KEY (exchange_id) REFERENCES stock_exchanges (id) ON DELETE RESTRICT,
    CONSTRAINT uq_stocks_exchange_symbol UNIQUE (exchange_id, symbol),
    CONSTRAINT uq_stocks_instrument_key UNIQUE (instrument_key)
);

CREATE INDEX idx_stocks_symbol ON stocks (symbol);
CREATE INDEX idx_stocks_company_name ON stocks (company_name);
CREATE INDEX idx_stocks_status ON stocks (status);

-- Persisted OHLC candles only (Part 10: never one row per tick). data_type distinguishes END_OF_DAY
-- (one row/day from the daily sync job) from INTRADAY (coarse, configurable-interval aggregates).
CREATE TABLE stock_prices (
    id                 UUID PRIMARY KEY,
    stock_id           UUID          NOT NULL,
    price_timestamp    TIMESTAMPTZ   NOT NULL,
    open                NUMERIC(14, 4),
    high                NUMERIC(14, 4),
    low                 NUMERIC(14, 4),
    close               NUMERIC(14, 4),
    last_traded_price   NUMERIC(14, 4),
    volume              BIGINT,
    source              VARCHAR(50)   NOT NULL,
    data_type           VARCHAR(20)   NOT NULL,
    created_at          TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT fk_stock_prices_stock FOREIGN KEY (stock_id) REFERENCES stocks (id) ON DELETE CASCADE,
    CONSTRAINT uq_stock_prices_stock_ts_type UNIQUE (stock_id, price_timestamp, data_type)
);

CREATE INDEX idx_stock_prices_stock_ts ON stock_prices (stock_id, price_timestamp);
CREATE INDEX idx_stock_prices_data_type ON stock_prices (data_type);

CREATE TABLE stock_data_sync (
    id                 UUID PRIMARY KEY,
    provider           VARCHAR(100) NOT NULL,
    sync_type          VARCHAR(30)  NOT NULL,
    started_at         TIMESTAMPTZ  NOT NULL,
    completed_at       TIMESTAMPTZ,
    records_processed  INTEGER      NOT NULL DEFAULT 0,
    records_failed     INTEGER      NOT NULL DEFAULT 0,
    status             VARCHAR(20)  NOT NULL,
    error_message      VARCHAR(2000),
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_stock_data_sync_provider_type ON stock_data_sync (provider, sync_type);
CREATE INDEX idx_stock_data_sync_started_at ON stock_data_sync (started_at);

INSERT INTO stock_exchanges (id, code, name, country, timezone, status)
VALUES
    (gen_random_uuid(), 'NSE', 'National Stock Exchange of India', 'IN', 'Asia/Kolkata', 'ACTIVE'),
    (gen_random_uuid(), 'BSE', 'Bombay Stock Exchange', 'IN', 'Asia/Kolkata', 'ACTIVE');
