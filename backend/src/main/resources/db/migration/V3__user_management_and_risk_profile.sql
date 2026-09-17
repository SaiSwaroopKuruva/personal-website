-- Phase 2: user profile, verification, security, notification and risk profiling schema

ALTER TABLE users
    ADD COLUMN role                        VARCHAR(20)  NOT NULL DEFAULT 'USER',
    ADD COLUMN date_of_birth              DATE,
    ADD COLUMN gender                      VARCHAR(20),
    ADD COLUMN occupation                  VARCHAR(100),
    ADD COLUMN annual_income               NUMERIC(15, 2),
    ADD COLUMN monthly_expenses            NUMERIC(15, 2),
    ADD COLUMN city                        VARCHAR(100),
    ADD COLUMN state                       VARCHAR(100),
    ADD COLUMN country                     VARCHAR(100),
    ADD COLUMN postal_code                 VARCHAR(20),
    ADD COLUMN pan_number                  VARCHAR(10),
    ADD COLUMN aadhaar_last_four           VARCHAR(4),
    ADD COLUMN kyc_status                  VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    ADD COLUMN email_verified              BOOLEAN      NOT NULL DEFAULT false,
    ADD COLUMN mobile_verified             BOOLEAN      NOT NULL DEFAULT false,
    ADD COLUMN profile_completed           BOOLEAN      NOT NULL DEFAULT false,
    ADD COLUMN preferred_language          VARCHAR(10)  NOT NULL DEFAULT 'en',
    ADD COLUMN investment_experience       VARCHAR(20),
    ADD COLUMN investment_horizon          VARCHAR(20),
    ADD COLUMN monthly_investment_budget   NUMERIC(15, 2),
    ADD COLUMN notification_preferences    JSONB        NOT NULL DEFAULT '{}'::jsonb,
    ADD COLUMN profile_picture             VARCHAR(500),
    ADD COLUMN last_login                  TIMESTAMPTZ,
    ADD COLUMN status                      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    ADD COLUMN created_by                  VARCHAR(100),
    ADD COLUMN updated_by                  VARCHAR(100);

CREATE TABLE user_addresses (
    id               UUID PRIMARY KEY,
    user_id          UUID NOT NULL,
    type             VARCHAR(20)  NOT NULL,
    address_line_1   VARCHAR(255) NOT NULL,
    address_line_2   VARCHAR(255),
    city             VARCHAR(100) NOT NULL,
    state            VARCHAR(100) NOT NULL,
    country          VARCHAR(100) NOT NULL,
    postal_code      VARCHAR(20)  NOT NULL,
    is_default       BOOLEAN      NOT NULL DEFAULT false,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT fk_user_addresses_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
CREATE INDEX idx_user_addresses_user_id ON user_addresses (user_id);

CREATE TABLE user_devices (
    id            UUID PRIMARY KEY,
    user_id       UUID NOT NULL,
    device_name   VARCHAR(150),
    browser       VARCHAR(150),
    ip_address    VARCHAR(45),
    last_login    TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_user_devices_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
CREATE INDEX idx_user_devices_user_id ON user_devices (user_id);

-- Links a refresh token (session) to the device it was issued to, enabling per-device revocation
ALTER TABLE refresh_tokens
    ADD COLUMN device_id UUID REFERENCES user_devices (id) ON DELETE SET NULL;

CREATE TABLE email_verification_tokens (
    id          UUID PRIMARY KEY,
    user_id     UUID         NOT NULL,
    token       VARCHAR(255) NOT NULL,
    expires_at  TIMESTAMPTZ  NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT fk_email_verification_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_email_verification_tokens_token UNIQUE (token)
);
CREATE INDEX idx_email_verification_tokens_user_id ON email_verification_tokens (user_id);

CREATE TABLE password_reset_tokens (
    id          UUID PRIMARY KEY,
    user_id     UUID         NOT NULL,
    token       VARCHAR(255) NOT NULL,
    expires_at  TIMESTAMPTZ  NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT fk_password_reset_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_password_reset_tokens_token UNIQUE (token)
);
CREATE INDEX idx_password_reset_tokens_user_id ON password_reset_tokens (user_id);

-- Supports the "prevent reuse of last 5 passwords" rule
CREATE TABLE password_history (
    id              UUID PRIMARY KEY,
    user_id         UUID         NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT fk_password_history_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
CREATE INDEX idx_password_history_user_id ON password_history (user_id);

CREATE TABLE risk_assessment_results (
    id              UUID PRIMARY KEY,
    user_id         UUID         NOT NULL,
    score           INTEGER      NOT NULL,
    risk_level      VARCHAR(30)  NOT NULL,
    recommendation  JSONB        NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT fk_risk_assessment_results_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
CREATE INDEX idx_risk_assessment_results_user_id ON risk_assessment_results (user_id);

-- Audit trail for profile, security, password and risk actions (Part 14)
CREATE TABLE audit_logs (
    id           UUID PRIMARY KEY,
    user_id      UUID,
    action       VARCHAR(100) NOT NULL,
    details      VARCHAR(1000),
    ip_address   VARCHAR(45),
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT fk_audit_logs_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL
);
CREATE INDEX idx_audit_logs_user_id ON audit_logs (user_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs (created_at);
