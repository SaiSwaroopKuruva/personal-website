-- Phase 5: Portfolio management, transaction ledger, valuation snapshots and manual net-worth tracking.
-- Holdings are intentionally NOT persisted as a table: current units/cost-basis are always derived from
-- portfolio_transactions (the source of truth) by HoldingCalculationServiceImpl (FIFO), so they can never
-- drift from the ledger. See docs/financial-data-providers.md / repo memory for the documented policy.

CREATE TABLE portfolios (
    id             UUID PRIMARY KEY,
    user_id        UUID          NOT NULL,
    name           VARCHAR(150)  NOT NULL,
    description    VARCHAR(1000),
    base_currency  VARCHAR(3)    NOT NULL DEFAULT 'INR',
    is_default     BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    archived_at    TIMESTAMPTZ,
    CONSTRAINT fk_portfolios_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_portfolios_user_name UNIQUE (user_id, name)
);

CREATE INDEX idx_portfolios_user ON portfolios (user_id);

-- Auditable transaction ledger; the sole source of truth for units held and cost basis (Part 4.3/5).
CREATE TABLE portfolio_transactions (
    id                       UUID           PRIMARY KEY,
    portfolio_id             UUID           NOT NULL,
    asset_type               VARCHAR(20)    NOT NULL,
    stock_symbol             VARCHAR(50),
    stock_exchange           VARCHAR(20),
    mutual_fund_scheme_code  VARCHAR(50),
    transaction_type         VARCHAR(20)    NOT NULL,
    transaction_date         DATE           NOT NULL,
    quantity                 NUMERIC(20, 6) NOT NULL DEFAULT 0,
    price_per_unit           NUMERIC(18, 4),
    gross_amount             NUMERIC(18, 2) NOT NULL,
    fees                     NUMERIC(18, 2) NOT NULL DEFAULT 0,
    taxes                    NUMERIC(18, 2) NOT NULL DEFAULT 0,
    net_amount               NUMERIC(18, 2) NOT NULL,
    notes                    VARCHAR(1000),
    external_reference       VARCHAR(100),
    created_at               TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at               TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT fk_portfolio_tx_portfolio FOREIGN KEY (portfolio_id) REFERENCES portfolios (id) ON DELETE CASCADE,
    CONSTRAINT chk_portfolio_tx_asset_identifier CHECK (
        (asset_type = 'STOCK' AND stock_symbol IS NOT NULL) OR
        (asset_type = 'MUTUAL_FUND' AND mutual_fund_scheme_code IS NOT NULL)
    ),
    -- quantity may be negative only for ADJUSTMENT rows (a signed correction to units held); all other
    -- transaction types are validated as non-negative in the service layer (Part 24: no DB-level sign
    -- assumption because ADJUSTMENT is the only type that represents a downward, non-cash correction).
    CONSTRAINT chk_portfolio_tx_fees_non_negative CHECK (fees >= 0),
    CONSTRAINT chk_portfolio_tx_taxes_non_negative CHECK (taxes >= 0)
);

CREATE INDEX idx_portfolio_tx_portfolio_date ON portfolio_transactions (portfolio_id, transaction_date DESC);
CREATE INDEX idx_portfolio_tx_symbol ON portfolio_transactions (portfolio_id, asset_type, stock_symbol);
CREATE INDEX idx_portfolio_tx_scheme ON portfolio_transactions (portfolio_id, asset_type, mutual_fund_scheme_code);
CREATE INDEX idx_portfolio_tx_type ON portfolio_transactions (portfolio_id, transaction_type);

-- Historical valuation snapshots for performance charts (Part 4.4/16). One row per portfolio per date.
CREATE TABLE portfolio_valuation_snapshots (
    id                UUID           PRIMARY KEY,
    portfolio_id      UUID           NOT NULL,
    snapshot_date     DATE           NOT NULL,
    invested_value    NUMERIC(18, 2) NOT NULL,
    market_value      NUMERIC(18, 2) NOT NULL,
    realized_gain     NUMERIC(18, 2) NOT NULL DEFAULT 0,
    unrealized_gain   NUMERIC(18, 2) NOT NULL DEFAULT 0,
    is_complete       BOOLEAN        NOT NULL DEFAULT TRUE,
    calculated_at     TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT fk_portfolio_snapshot_portfolio FOREIGN KEY (portfolio_id) REFERENCES portfolios (id) ON DELETE CASCADE,
    CONSTRAINT uq_portfolio_snapshot_date UNIQUE (portfolio_id, snapshot_date)
);

CREATE INDEX idx_portfolio_snapshot_portfolio_date ON portfolio_valuation_snapshots (portfolio_id, snapshot_date);

-- Manually tracked assets/liabilities for net worth (Part 4.5). Never auto-populated (Part 15).
CREATE TABLE user_assets (
    id              UUID           PRIMARY KEY,
    user_id         UUID           NOT NULL,
    name            VARCHAR(150)   NOT NULL,
    category        VARCHAR(30)    NOT NULL,
    current_value   NUMERIC(18, 2) NOT NULL,
    currency        VARCHAR(3)     NOT NULL DEFAULT 'INR',
    valuation_date  DATE           NOT NULL,
    notes           VARCHAR(1000),
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT fk_user_assets_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT chk_user_assets_value_non_negative CHECK (current_value >= 0)
);

CREATE INDEX idx_user_assets_user ON user_assets (user_id);

CREATE TABLE user_liabilities (
    id              UUID           PRIMARY KEY,
    user_id         UUID           NOT NULL,
    name            VARCHAR(150)   NOT NULL,
    category        VARCHAR(30)    NOT NULL,
    current_value   NUMERIC(18, 2) NOT NULL,
    currency        VARCHAR(3)     NOT NULL DEFAULT 'INR',
    valuation_date  DATE           NOT NULL,
    notes           VARCHAR(1000),
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT fk_user_liabilities_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT chk_user_liabilities_value_non_negative CHECK (current_value >= 0)
);

CREATE INDEX idx_user_liabilities_user ON user_liabilities (user_id);
