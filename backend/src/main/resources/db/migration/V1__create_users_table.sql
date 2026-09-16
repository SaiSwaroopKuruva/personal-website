CREATE TABLE users (
    id              UUID PRIMARY KEY,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    email           VARCHAR(255) NOT NULL,
    mobile          VARCHAR(20)  NOT NULL,
    password        VARCHAR(255) NOT NULL,
    risk_profile    VARCHAR(20)  NOT NULL DEFAULT 'MODERATE',
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT uq_users_mobile UNIQUE (mobile)
);

CREATE INDEX idx_users_email ON users (email);
