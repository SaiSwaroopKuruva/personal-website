-- Phase 3: Mutual Fund discovery & analysis platform schema

CREATE TABLE mutual_fund_amcs (
    id            UUID PRIMARY KEY,
    name          VARCHAR(200) NOT NULL,
    short_name    VARCHAR(100),
    code          VARCHAR(50)  NOT NULL,
    logo_url      VARCHAR(500),
    website_url   VARCHAR(500),
    description   VARCHAR(2000),
    status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_mutual_fund_amcs_code UNIQUE (code)
);

CREATE TABLE mutual_funds (
    id                     UUID PRIMARY KEY,
    scheme_code            VARCHAR(50)   NOT NULL,
    isin                   VARCHAR(20),
    amc_id                 UUID          NOT NULL,
    scheme_name            VARCHAR(300)  NOT NULL,
    short_name             VARCHAR(150),
    category               VARCHAR(100)  NOT NULL,
    sub_category           VARCHAR(100),
    plan_type              VARCHAR(20)   NOT NULL,
    option_type            VARCHAR(20)   NOT NULL,
    asset_class            VARCHAR(50)   NOT NULL,
    investment_objective   VARCHAR(2000),
    risk_level             VARCHAR(30)   NOT NULL,
    benchmark              VARCHAR(200),
    expense_ratio          NUMERIC(5, 2),
    exit_load              VARCHAR(500),
    minimum_lumpsum        NUMERIC(12, 2),
    minimum_sip            NUMERIC(12, 2),
    aum                    NUMERIC(18, 2),
    nav                    NUMERIC(12, 4),
    nav_date               DATE,
    inception_date         DATE,
    fund_manager           VARCHAR(200),
    status                 VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
    created_at             TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at             TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT fk_mutual_funds_amc FOREIGN KEY (amc_id) REFERENCES mutual_fund_amcs (id) ON DELETE RESTRICT,
    CONSTRAINT uq_mutual_funds_scheme_code UNIQUE (scheme_code),
    CONSTRAINT uq_mutual_funds_isin UNIQUE (isin)
);

CREATE INDEX idx_mutual_funds_amc_id ON mutual_funds (amc_id);
CREATE INDEX idx_mutual_funds_category ON mutual_funds (category);
CREATE INDEX idx_mutual_funds_sub_category ON mutual_funds (sub_category);
CREATE INDEX idx_mutual_funds_risk_level ON mutual_funds (risk_level);
CREATE INDEX idx_mutual_funds_nav_date ON mutual_funds (nav_date);
CREATE INDEX idx_mutual_funds_scheme_name ON mutual_funds (scheme_name);
CREATE INDEX idx_mutual_funds_status ON mutual_funds (status);

CREATE TABLE mutual_fund_nav_history (
    id              UUID PRIMARY KEY,
    mutual_fund_id  UUID        NOT NULL,
    nav             NUMERIC(12, 4) NOT NULL,
    nav_date        DATE        NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_mutual_fund_nav_history_fund FOREIGN KEY (mutual_fund_id) REFERENCES mutual_funds (id) ON DELETE CASCADE,
    CONSTRAINT uq_mutual_fund_nav_history_fund_date UNIQUE (mutual_fund_id, nav_date)
);

-- Composite index backs efficient "history for fund X between date range, ordered" chart queries
CREATE INDEX idx_mutual_fund_nav_history_fund_date ON mutual_fund_nav_history (mutual_fund_id, nav_date);

CREATE TABLE mutual_fund_holdings (
    id                 UUID PRIMARY KEY,
    mutual_fund_id     UUID          NOT NULL,
    security_name      VARCHAR(300)  NOT NULL,
    isin               VARCHAR(20),
    sector             VARCHAR(150),
    asset_type         VARCHAR(50)   NOT NULL,
    weight_percentage  NUMERIC(5, 2) NOT NULL,
    quantity           NUMERIC(18, 4),
    market_value       NUMERIC(18, 2),
    as_of_date         DATE          NOT NULL,
    created_at         TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT fk_mutual_fund_holdings_fund FOREIGN KEY (mutual_fund_id) REFERENCES mutual_funds (id) ON DELETE CASCADE
);

CREATE INDEX idx_mutual_fund_holdings_fund_id ON mutual_fund_holdings (mutual_fund_id);
CREATE INDEX idx_mutual_fund_holdings_fund_as_of_date ON mutual_fund_holdings (mutual_fund_id, as_of_date);
CREATE INDEX idx_mutual_fund_holdings_sector ON mutual_fund_holdings (sector);

CREATE TABLE mutual_fund_managers (
    id                UUID PRIMARY KEY,
    mutual_fund_id    UUID         NOT NULL,
    name              VARCHAR(200) NOT NULL,
    experience_years  INTEGER,
    joining_date      DATE,
    designation       VARCHAR(150),
    bio               VARCHAR(2000),
    active            BOOLEAN      NOT NULL DEFAULT true,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT fk_mutual_fund_managers_fund FOREIGN KEY (mutual_fund_id) REFERENCES mutual_funds (id) ON DELETE CASCADE
);

CREATE INDEX idx_mutual_fund_managers_fund_id ON mutual_fund_managers (mutual_fund_id);

CREATE TABLE mutual_fund_returns (
    id                 UUID PRIMARY KEY,
    mutual_fund_id     UUID          NOT NULL,
    return_period      VARCHAR(20)   NOT NULL,
    return_percentage  NUMERIC(9, 4) NOT NULL,
    annualized         BOOLEAN       NOT NULL DEFAULT true,
    calculated_as_of   DATE          NOT NULL,
    created_at         TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT fk_mutual_fund_returns_fund FOREIGN KEY (mutual_fund_id) REFERENCES mutual_funds (id) ON DELETE CASCADE,
    CONSTRAINT uq_mutual_fund_returns_fund_period UNIQUE (mutual_fund_id, return_period)
);

CREATE INDEX idx_mutual_fund_returns_fund_id ON mutual_fund_returns (mutual_fund_id);

CREATE TABLE user_mutual_fund_favorites (
    id              UUID PRIMARY KEY,
    user_id         UUID        NOT NULL,
    mutual_fund_id  UUID        NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_user_mutual_fund_favorites_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_mutual_fund_favorites_fund FOREIGN KEY (mutual_fund_id) REFERENCES mutual_funds (id) ON DELETE CASCADE,
    CONSTRAINT uq_user_mutual_fund_favorites_user_fund UNIQUE (user_id, mutual_fund_id)
);

CREATE INDEX idx_user_mutual_fund_favorites_user_id ON user_mutual_fund_favorites (user_id);

CREATE TABLE mutual_fund_data_sync (
    id                 UUID PRIMARY KEY,
    provider           VARCHAR(100) NOT NULL,
    sync_type          VARCHAR(30)  NOT NULL,
    started_at         TIMESTAMPTZ  NOT NULL,
    completed_at       TIMESTAMPTZ,
    records_processed  INTEGER      NOT NULL DEFAULT 0,
    records_failed     INTEGER      NOT NULL DEFAULT 0,
    status             VARCHAR(20)  NOT NULL,
    error_message       VARCHAR(2000),
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_mutual_fund_data_sync_provider_type ON mutual_fund_data_sync (provider, sync_type);
CREATE INDEX idx_mutual_fund_data_sync_started_at ON mutual_fund_data_sync (started_at);
