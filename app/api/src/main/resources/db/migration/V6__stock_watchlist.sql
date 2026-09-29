-- Phase 4: user stock watchlist (mirrors user_mutual_fund_favorites pattern)

CREATE TABLE user_stock_watchlist (
    id          UUID PRIMARY KEY,
    user_id     UUID        NOT NULL,
    stock_id    UUID        NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_watchlist_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_watchlist_stock FOREIGN KEY (stock_id) REFERENCES stocks (id) ON DELETE CASCADE,
    CONSTRAINT uq_watchlist_user_stock UNIQUE (user_id, stock_id)
);

CREATE INDEX idx_watchlist_user ON user_stock_watchlist (user_id);
