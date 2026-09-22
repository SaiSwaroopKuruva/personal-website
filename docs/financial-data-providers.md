# Financial Data Providers (Phase 3.5)

This document describes how FinAdvisor integrates **real** financial market data: Upstox for stock market
data and AMFI for mutual fund NAV data. It replaces the Phase 3 demo mutual fund provider and adds a
brand-new stock market data feature. Phase 1/2/3/4 functionality is unchanged.

> **Disclaimer:** Market and mutual fund data may be delayed, subject to provider availability, data-source
> limitations, licensing restrictions, and synchronization intervals. This platform provides informational
> data and calculators and does not constitute investment advice. This platform does not support placing
> trades, orders, or mutual fund purchases/redemptions.

## 1. Architecture

```
GitHub Pages (Next.js static export)
        |
        | HTTPS
        v
   Render (Spring Boot API)
        |
        +----> Upstox (stocks, read-only Analytics Token)
        |
        +----> AMFI (mutual fund NAV, public text file)
        |
        +----> In-memory cache (per-provider TTL cache)
        |
        +----> Neon PostgreSQL (Flyway-managed schema)
```

Controllers never talk to Upstox/AMFI directly:

```
Controller -> Service -> Provider interface -> Provider implementation
```

- `finadvisor.stock.provider.StockMarketDataProvider` - interface. Implementation: `UpstoxStockMarketDataProvider`
  (`finadvisor.stock.provider.upstox`), activated via `STOCK_DATA_PROVIDER=UPSTOX`.
- `finadvisor.mutualfund.provider.MutualFundDataProvider` - interface (unchanged from Phase 3). Implementations:
  `DemoMutualFundDataProvider` (dev-only, default) and the new `AmfiMutualFundDataProvider`
  (`finadvisor.mutualfund.provider.amfi`), activated via `MUTUAL_FUND_PROVIDER_NAME=amfi`.

Both providers are optional Spring beans (`@ConditionalOnProperty`); the application starts and serves all
other functionality even if a provider is not configured. Stock endpoints return `503 Service Unavailable`
with a clear message rather than fabricated data when no stock provider is configured.

## 2. Upstox stock data integration

### Authentication

Upstox's **Analytics Token** (https://upstox.com/developer/api-documentation/analytics-token) is a
long-lived (1 year), read-only, free token generated once from the Upstox Developer Apps dashboard - no
OAuth redirect flow, no `client_id`/`client_secret`/`redirect_uri`, no brokerage account linking. It grants
read-only access to Market Data, Realtime & Streaming, and (with a whitelisted static IP) read-only
Portfolio/Account APIs. It **cannot** place, modify or cancel orders.

This platform only uses the Market Data category of the Analytics Token - no static IP is required for any
endpoint used here.

The token is read once from `UPSTOX_ANALYTICS_TOKEN` into `UpstoxProperties` and attached as an
`Authorization: Bearer` header by `UpstoxApiClient`. It is never logged, never returned in an API response,
and never placed in a `NEXT_PUBLIC_*` frontend variable.

### Endpoints used (verified against current Upstox documentation)

| Capability | Endpoint |
|---|---|
| Search stocks | `GET /v2/instruments/search?query=...&exchanges=NSE&segments=EQ` |
| Current price / quote | `GET /v3/market-quote/quotes?instrument_key=...` (Full Market Quote V3) |
| Historical OHLC | `GET /v3/historical-candle/{instrument_key}/days/1/{to}/{from}` |
| Intraday OHLC | `GET /v3/historical-candle/{instrument_key}/minutes/1/{today}/{today}` (see limitation below) |
| Market indices | `GET /v3/market-quote/quotes?instrument_key=NSE_INDEX|Nifty 50,...` |
| Market status | `GET /v2/market/status/{exchange}` |
| News | `GET /v2/news?category=instrument_keys&instrument_keys=...` |

### Known Upstox limitations (documented, not silently worked around)

- **No dedicated "top gainers/losers/most active" screener endpoint exists.** `GET /api/market/gainers`,
  `/losers` and `/most-active` are computed by this platform from real Full Market Quote V3 responses across
  a bounded, configurable "tracked symbol universe" (`stock.provider.upstox.tracked-symbols`, default: ~20
  large-cap NSE stocks). This is real, provider-sourced data - never fabricated - but it is not a full-exchange
  screener. Expanding the tracked list (or ingesting the full NSE instrument file, see below) increases coverage.
- **No dedicated "intraday" v3 endpoint is documented separately from the historical-candle v3 endpoint.**
  Intraday 1-minute candles are fetched by calling the same v3 historical-candle endpoint with `from=to=today`,
  which Upstox's documentation confirms serves minute-level data for the current trading day.
- **Fundamentals (financial statements, key ratios, dividends, corporate actions) are NOT implemented in this
  phase.** Upstox does expose a Fundamentals API family (Company Profile, Balance Sheet, Cash Flow, Income
  Statement, Share Holdings, Key Ratios, Corporate Actions - all keyed by ISIN), but their exact response
  schemas were not verified against a live account in this phase, so `GET /api/stocks/{symbol}/metrics`,
  `/financials`, `/dividends`, `/corporate-actions` intentionally return `501 Not Implemented` rather than a
  guessed/incorrect mapping. This is the primary candidate for Phase 5.
- **No free-text instrument master ingestion.** Upstox publishes a full NSE/BSE instrument JSON file
  (`https://assets.upstox.com/market-quote/instruments/exchange/NSE.json.gz`), but this phase uses the
  `Instrument Search` REST API (real-time, verified, paginated) instead of downloading/parsing that ~tens-of-MB
  file, to keep the initial implementation simple. Search coverage is therefore live (any NSE equity can be
  found), but the *tracked/synced* universe persisted into the `stocks` table is intentionally bounded (Part 40:
  never fetch the entire exchange on every request).
- **WebSocket real-time streaming is not implemented in this phase.** Upstox's V3 market-data WebSocket feed
  uses a binary Protobuf message format. `StockMarketDataProvider.connectMarketStream()/subscribe()/unsubscribe()/disconnect()`
  exist as default interface methods that throw `UNSUPPORTED_OPERATION` so the architecture is ready for a
  future `UpstoxWebSocketMarketDataStreamService` (Phase 5) without any interface changes.

### Data normalization

Every stock response is normalized into `finadvisor.stock.provider.MarketQuote` before it reaches a
controller, carrying `dataType` (`REAL_TIME`/`INTRADAY`/`DELAYED`/`END_OF_DAY`) and `freshness`
(`LIVE`/`FRESH`/`STALE`/`UNKNOWN`). `freshness` is derived from how old the provider's own quote timestamp is
(≤5 min = `LIVE`, ≤24h = `FRESH`, otherwise `STALE`) - never hard-coded to `LIVE`.

### Persistence strategy

- Live quotes/movers/indices are **not** persisted per request (avoids "one row per tick").
- `stock_prices` only stores `END_OF_DAY` candles, written once per trading day by the scheduled/admin EOD
  sync job (`StockDataSyncServiceImpl.syncEndOfDayPrices()`), which fetches the most recent finalized daily
  candle via the historical-candle API (not a same-day close that may not exist yet).
- `stock_data_sync` records every sync run (provider, type, counts, status, error) for observability.

## 3. AMFI mutual fund data integration

### Data source

AMFI publishes a daily plain-text NAV file (semicolon-separated, grouped by broad scheme category then AMC)
at a well-known public URL, configurable via `AMFI_BASE_URL` (default
`https://www.amfiindia.com/spragmt/NAVAll.txt`). No credentials are required - this is a free, public
data source, not authenticated in any way.

`AmfiNavParser` parses this format defensively: malformed rows (invalid NAV, invalid date, missing AMC
context) are skipped and counted rather than failing the whole sync (Part 14). `AmfiMutualFundDataProvider`
caches the parsed snapshot in memory for `mutualfund.provider.amfi.cache-ttl-minutes` (default 60) to avoid
re-fetching AMFI's servers on every request.

### Known AMFI limitations (documented, not silently worked around)

- **AMFI publishes only the latest NAV snapshot, not a per-scheme historical NAV REST API.**
  `AmfiMutualFundDataProvider.getHistoricalNav(...)` returns a clearly labeled failure explaining this;
  historical NAV is instead **accumulated over time** by the existing Phase 3 scheduled `NAV_SYNC` job, which
  appends each day's NAV into `mutual_fund_nav_history`. Historical charts will only show data from the point
  synchronization was enabled onward, unless separately backfilled.
- **AMFI does not publish holdings, fund managers, or trailing returns.** `getHoldings`, `getFundManagers`
  and `getFundReturns` return a clear "not supported by this provider" failure rather than fabricated data.
  These would require a separate, richer data source (e.g. AMC fact sheets or a commercial data vendor).
- **AMFI does not classify plan type, option type, or SEBI riskometer risk level as separate fields.** These
  are inferred heuristically from the scheme name/category text (documented in `AmfiMutualFundDataProvider`);
  risk level defaults to the new `FundRiskLevel.NOT_RATED` enum value rather than guessing a risk band AMFI
  never actually assigned.
- **AMC code is derived, not provided.** AMFI gives only the AMC's display name (e.g. "Axis Mutual Fund");
  a stable `amcCode` is deterministically slugified from that name so repeated syncs map to the same row.

### Data normalization

`AmfiMutualFundDataProvider` maps every row into the existing `ProviderFundData`/`ProviderNavPoint` records
(unchanged from Phase 3) - no changes to `MutualFundDataProvider`, `MutualFundServiceImpl`,
`MutualFundDataSyncServiceImpl`, or any REST endpoint were required. Switching between the demo provider and
AMFI is a single environment variable (`MUTUAL_FUND_PROVIDER_NAME=amfi`).

## 4. Environment variables

See `app/api/.env.example` for the full annotated list. Summary of what's new in Phase 3.5:

| Variable | Purpose | Required for |
|---|---|---|
| `MUTUAL_FUND_PROVIDER_NAME` | `demo` (default) or `amfi` | Switching to real mutual fund data |
| `AMFI_BASE_URL` | AMFI NAVAll.txt URL | AMFI provider (has a sensible default) |
| `AMFI_CACHE_TTL_MINUTES` | In-memory NAV snapshot TTL | AMFI provider (optional) |
| `STOCK_DATA_PROVIDER` | Unset (no stock data) or `UPSTOX` | Enabling stock data |
| `STOCK_PROVIDER_ENABLED` | Gates scheduled stock sync jobs | Scheduled sync |
| `UPSTOX_API_BASE_URL` | Upstox API base URL | Upstox provider (has a default) |
| `UPSTOX_ANALYTICS_TOKEN` | Read-only Analytics Token | Upstox provider (**required**, server-side only) |
| `UPSTOX_CACHE_TTL_MINUTES` | Tracked-instrument cache TTL | Upstox provider (optional) |
| `STOCK_INSTRUMENT_SYNC_CRON` / `STOCK_EOD_SYNC_CRON` | Scheduled sync cron expressions | Optional, `-` disables |
| `MUTUAL_FUND_SYNC_CRON` | Existing Phase 3 variable, unchanged | Optional |

`NEXT_PUBLIC_API_URL` (frontend) is the **only** stock/mutual-fund-related variable the frontend ever sees -
it must contain only the Render backend base URL, never a provider token.

## 5. Local setup

1. Copy `app/api/.env.example` values into your local environment (or `application-local.properties`, which
   is git-ignored).
2. To use real data locally: generate an Upstox Analytics Token (see below) and set
   `STOCK_DATA_PROVIDER=UPSTOX` + `UPSTOX_ANALYTICS_TOKEN=...`; set `MUTUAL_FUND_PROVIDER_NAME=amfi` for real
   mutual fund data (AMFI needs no credentials).
3. Leaving both unset is fine - the app runs with the Phase 3 demo mutual fund provider and stock endpoints
   returning a clear "provider not configured" response.
4. Run `docker compose -f infrastructure/docker-compose.yml up` or run the backend/frontend directly (see
   root `README.md`).

## 6. Upstox setup

1. Create an app at https://account.upstox.com/developer/apps (any app type - trading OAuth fields are not
   used by this platform).
2. Go to the **Analytics** tab, click **Generate Token**, confirm, and copy the token immediately (it is
   only shown once, truncated afterward).
3. Set `UPSTOX_ANALYTICS_TOKEN` in your environment (local `.env`/`application-local.properties`, or the
   Render dashboard for production). Never commit it.
4. The token is valid for 1 year and does not need daily regeneration; rotate it by generating a new token
   and updating the environment variable (only one Analytics Token is permitted per account at a time).

## 7. AMFI setup

No account or credentials are required. Set `MUTUAL_FUND_PROVIDER_NAME=amfi`. Optionally override
`AMFI_BASE_URL` if AMFI changes their published file location.

## 8. Neon setup

Unchanged from Phase 1/2/3: set `DATABASE_URL`/`DATABASE_USERNAME`/`DATABASE_PASSWORD` to your Neon
connection details (`sslmode=require`). Flyway runs `V5__stock_market_platform.sql` automatically on
startup, adding the new `stock_exchanges`, `stocks`, `stock_prices` and `stock_data_sync` tables - existing
tables/data are untouched.

## 9. Render setup

Add the new environment variables listed in section 4 to the Render service (see
`infrastructure/render.yaml`, which now includes them with `sync: false` for secrets). Leave
`STOCK_DATA_PROVIDER`/`MUTUAL_FUND_PROVIDER_NAME` unset (or `STOCK_PROVIDER_ENABLED=false`) if you are not
ready to enable real providers yet - the API remains fully functional otherwise.

## 10. GitHub Pages setup

No changes. The frontend still only knows `NEXT_PUBLIC_API_URL` (the Render backend URL) and never talks to
Upstox, AMFI or Neon directly.

## 11. Data freshness

Every stock quote response carries `source`, `dataType` and `freshness` (see `StockQuoteResponse`). The
frontend (`FreshnessBadge`/`DataSourceNote` components) renders these directly rather than assuming
liveness. Mutual fund NAV freshness is represented by the existing `navDate` field plus the new
`GET /api/data-providers/status` endpoint, which reports each provider's connection status and last
successful update time (`ProviderStatusBanner` component on the Stocks/Market pages).

## 12. Rate limits

Per Upstox's documented limits (https://upstox.com/developer/api-documentation/rate-limiting), "Other
Standard APIs" (which include Market Quote/Historical Data used here) allow 50 requests/second, 500/minute,
2000/30 minutes per user/token. This platform stays well within those limits by:
- Batching quote requests for the whole tracked universe into a single call (comma-separated
  `instrument_key` list, up to 500 instruments per Upstox call).
- Caching the tracked-instrument catalog (`UPSTOX_CACHE_TTL_MINUTES`, default 30 minutes) instead of
  re-resolving symbols on every request.
- Only persisting EOD candles once/day via the scheduled sync job, not polling continuously.

AMFI has no published rate limit, but this platform still caches the parsed snapshot
(`AMFI_CACHE_TTL_MINUTES`, default 60 minutes) to avoid unnecessary repeated downloads of the full NAV file.

## 13. Caching

Reuses the existing in-memory `ConcurrentMapCacheManager` (`finadvisor.mutualfund.config.CacheConfig`) for
mutual fund data (unchanged). Stock provider caching (tracked-instrument resolution) is implemented inside
`UpstoxStockMarketDataProvider` itself, not through the generic Spring cache abstraction, because live quotes
must never be served from an unbounded-TTL cache as if they were fresh. If cache is unavailable/cold, calls
simply go to the provider directly - the system still functions.

## 14. Synchronization

| Job | Default schedule | Trigger |
|---|---|---|
| Mutual fund full/NAV sync | Disabled (`-`) | `MUTUAL_FUND_SYNC_CRON` or `POST /api/admin/mutual-funds/sync[/nav]` |
| Stock instrument sync | Disabled (`-`) | `STOCK_INSTRUMENT_SYNC_CRON` or `POST /api/admin/stocks/sync/instruments` |
| Stock EOD price sync | Disabled (`-`) | `STOCK_EOD_SYNC_CRON` or `POST /api/admin/stocks/sync/eod` |

All scheduled jobs are additionally gated by `*_PROVIDER_ENABLED=true` and catch/log all exceptions so a
provider outage never crashes the application.

## 15. Security

- `UPSTOX_ANALYTICS_TOKEN` is read only via `@ConfigurationProperties`, attached as a Bearer header
  server-side, and never logged (see `UpstoxApiClient` - only `provider=UPSTOX operation=... status=...`
  structured log lines are emitted, no header values).
- CORS remains restricted to `CORS_ALLOWED_ORIGINS` (no `*` in production).
- All new public GET endpoints (`/api/stocks/**`, `/api/market/**`, `/api/data-providers/status`) are
  read-only informational data; admin sync endpoints require `ROLE_ADMIN` (`@PreAuthorize`), matching the
  existing mutual fund admin sync pattern.
- `.gitignore` already excludes `.env*` files; no changes needed.

## 16. Troubleshooting

| Symptom | Likely cause |
|---|---|
| `GET /api/stocks/...` returns 503 | `STOCK_DATA_PROVIDER` is unset/not `UPSTOX`, or `UPSTOX_ANALYTICS_TOKEN` is blank |
| `401` from Upstox (surfaced as 401 to the client) | Analytics Token invalid/expired/revoked - regenerate it |
| `429` from Upstox (surfaced as 429) | Rate limit exceeded - reduce tracked-symbol count or sync frequency |
| Mutual fund sync always fails with 0 records | `AMFI_BASE_URL` unreachable or AMFI changed the file format/URL |
| Stock/mutual fund provider shows `DISABLED` in `/api/data-providers/status` | `*_PROVIDER_ENABLED=false` (informational only - the provider can still serve live reads even when scheduled sync is disabled) |

## 17. Provider limitations summary

See sections 2 and 3 above. In short: no Upstox gainers/losers/most-active screener (computed locally), no
Upstox Fundamentals integration yet (financials/dividends/corporate-actions return `501`), no WebSocket
streaming yet, and no AMFI historical-NAV/holdings/managers/returns API (accumulated locally or unsupported).

## 18. How to replace providers

Both interfaces (`StockMarketDataProvider`, `MutualFundDataProvider`) are designed for exactly this:

1. Implement the interface in a new class annotated `@ConditionalOnProperty(prefix = "...", name = "name", havingValue = "<your-provider>")`.
2. Point `STOCK_DATA_PROVIDER` / `MUTUAL_FUND_PROVIDER_NAME` at your new provider's value.
3. No controller, service, or database changes are required - the existing sync services, caches, and REST
   APIs are provider-agnostic.
