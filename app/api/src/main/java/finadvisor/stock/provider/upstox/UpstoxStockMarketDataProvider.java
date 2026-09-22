package finadvisor.stock.provider.upstox;

import com.fasterxml.jackson.databind.JsonNode;
import finadvisor.marketdata.DataFreshness;
import finadvisor.marketdata.DataType;
import finadvisor.stock.provider.MarketCandle;
import finadvisor.stock.provider.MarketIndex;
import finadvisor.stock.provider.MarketQuote;
import finadvisor.stock.provider.MarketStatusInfo;
import finadvisor.stock.provider.NewsItem;
import finadvisor.stock.provider.ProviderResponse;
import finadvisor.stock.provider.ProviderStockDetails;
import finadvisor.stock.provider.StockMarketDataProvider;
import finadvisor.stock.provider.StockProviderException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Real stock market-data provider backed by Upstox's read-only Analytics Token market-data APIs (Part 5/6).
 * Activate with {@code STOCK_DATA_PROVIDER=UPSTOX}. Uses documented v2/v3 REST endpoints only - see
 * docs/financial-data-providers.md for the exact endpoints used and known limitations (no free-text
 * gainers/losers/most-active screener endpoint exists, so those are computed locally from real quotes
 * across this deployment's tracked symbol universe; WebSocket streaming is not implemented in this phase).
 */
@Component
@ConditionalOnProperty(prefix = "stock.provider", name = "name", havingValue = "UPSTOX")
public class UpstoxStockMarketDataProvider implements StockMarketDataProvider {

    private static final Logger log = Logger.getLogger(UpstoxStockMarketDataProvider.class.getName());
    private static final String PROVIDER_NAME = "UPSTOX";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final int TOP_MOVERS_LIMIT = 10;

    private final UpstoxApiClient apiClient;
    private final UpstoxProperties properties;
    private final Map<String, ProviderStockDetails> trackedInstruments = new ConcurrentHashMap<>();
    private volatile Instant trackedCacheRefreshedAt = Instant.EPOCH;

    public UpstoxStockMarketDataProvider(UpstoxApiClient apiClient, UpstoxProperties properties) {
        this.apiClient = apiClient;
        this.properties = properties;
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public ProviderResponse<List<ProviderStockDetails>> searchStocks(String query) {
        if (query == null || query.isBlank()) {
            return ProviderResponse.failure("A non-empty search query is required", PROVIDER_NAME);
        }
        try {
            String encodedQuery = java.net.URLEncoder.encode(query.strip(), java.nio.charset.StandardCharsets.UTF_8);
            JsonNode data = apiClient.getData("SEARCH_INSTRUMENTS",
                    "/v2/instruments/search?query=" + encodedQuery + "&exchanges=NSE&segments=EQ&records=20");
            List<ProviderStockDetails> results = new ArrayList<>();
            for (JsonNode item : data) {
                results.add(toStockDetails(item));
            }
            return ProviderResponse.ok(results, PROVIDER_NAME);
        } catch (StockProviderException ex) {
            return ProviderResponse.failure(ex.getMessage(), PROVIDER_NAME);
        }
    }

    @Override
    public ProviderResponse<ProviderStockDetails> getStockDetails(String symbol) {
        return resolve(symbol)
                .map(details -> ProviderResponse.ok(details, PROVIDER_NAME))
                .orElseGet(() -> ProviderResponse.failure("Unknown or untracked symbol: " + symbol, PROVIDER_NAME));
    }

    @Override
    public ProviderResponse<MarketQuote> getCurrentPrice(String symbol) {
        Optional<ProviderStockDetails> details = resolve(symbol);
        if (details.isEmpty()) {
            return ProviderResponse.failure("Unknown or untracked symbol: " + symbol, PROVIDER_NAME);
        }
        JsonNode data = apiClient.getData("GET_QUOTE",
                "/v3/market-quote/quotes?instrument_key=" + encodeInstrumentKey(details.get().instrumentKey()));
        JsonNode entry = firstValue(data);
        if (entry == null) {
            return ProviderResponse.failure("Upstox returned no quote for " + symbol, PROVIDER_NAME);
        }
        return ProviderResponse.ok(toMarketQuote(details.get(), entry), PROVIDER_NAME);
    }

    @Override
    public ProviderResponse<List<MarketCandle>> getHistoricalPrices(String symbol, LocalDate from, LocalDate to) {
        Optional<ProviderStockDetails> details = resolve(symbol);
        if (details.isEmpty()) {
            return ProviderResponse.failure("Unknown or untracked symbol: " + symbol, PROVIDER_NAME);
        }
        if (from == null || to == null || from.isAfter(to)) {
            return ProviderResponse.failure("'from' must be on/before 'to'", PROVIDER_NAME);
        }
        String instrumentKey = details.get().instrumentKey();
        JsonNode data = apiClient.getData("GET_HISTORICAL_CANDLES", uriBuilder -> uriBuilder
                .path("/v3/historical-candle/{instrumentKey}/{unit}/{interval}/{toDate}/{fromDate}")
                .build(instrumentKey, "days", 1, to.format(DATE_FORMAT), from.format(DATE_FORMAT)));
        return ProviderResponse.ok(parseCandles(data), PROVIDER_NAME);
    }

    @Override
    public ProviderResponse<List<MarketCandle>> getIntradayPrices(String symbol) {
        Optional<ProviderStockDetails> details = resolve(symbol);
        if (details.isEmpty()) {
            return ProviderResponse.failure("Unknown or untracked symbol: " + symbol, PROVIDER_NAME);
        }
        String instrumentKey = details.get().instrumentKey();
        LocalDate today = LocalDate.now();
        // The v3 historical-candle endpoint also serves the current trading day at 1-minute granularity,
        // which we reuse here rather than guessing at an undocumented dedicated "intraday" v3 path.
        JsonNode data = apiClient.getData("GET_INTRADAY_CANDLES", uriBuilder -> uriBuilder
                .path("/v3/historical-candle/{instrumentKey}/{unit}/{interval}/{toDate}/{fromDate}")
                .build(instrumentKey, "minutes", 1, today.format(DATE_FORMAT), today.format(DATE_FORMAT)));
        return ProviderResponse.ok(parseCandles(data), PROVIDER_NAME);
    }

    @Override
    public ProviderResponse<List<MarketIndex>> getMarketIndices() {
        String joined = String.join(",", properties.getIndexInstrumentKeys().stream().map(this::encodeInstrumentKey).toList());
        JsonNode data = apiClient.getData("GET_MARKET_INDICES", "/v3/market-quote/quotes?instrument_key=" + joined);
        List<MarketIndex> indices = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> fields = data.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            JsonNode entry = field.getValue();
            BigDecimal lastPrice = decimalOrNull(entry, "last_price");
            BigDecimal prevClose = decimalOrNull(entry, "prev_close_price");
            BigDecimal change = decimalOrNull(entry, "net_change");
            BigDecimal changePercent = percent(change, prevClose);
            indices.add(new MarketIndex(field.getKey(), entry.path("instrument_token").asText(null), lastPrice, change,
                    changePercent, parseTimestamp(entry)));
        }
        return ProviderResponse.ok(indices, PROVIDER_NAME);
    }

    @Override
    public ProviderResponse<List<MarketQuote>> getTopGainers() {
        return topMovers(Comparator.comparing(MarketQuote::changePercent, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
    }

    @Override
    public ProviderResponse<List<MarketQuote>> getTopLosers() {
        return topMovers(Comparator.comparing(MarketQuote::changePercent, Comparator.nullsLast(Comparator.naturalOrder())));
    }

    @Override
    public ProviderResponse<List<MarketQuote>> getMostActive() {
        return topMovers(Comparator.comparing((MarketQuote q) -> q.volume() == null ? 0L : q.volume()).reversed());
    }

    @Override
    public ProviderResponse<MarketStatusInfo> getMarketStatus(String exchange) {
        String ex = (exchange == null || exchange.isBlank()) ? "NSE" : exchange.strip().toUpperCase(Locale.ROOT);
        JsonNode data = apiClient.getData("GET_MARKET_STATUS", "/v2/market/status/" + ex);
        Instant lastUpdated = data.hasNonNull("last_updated") ? Instant.ofEpochMilli(data.get("last_updated").asLong()) : null;
        return ProviderResponse.ok(new MarketStatusInfo(data.path("exchange").asText(ex), data.path("status").asText("UNKNOWN"), lastUpdated), PROVIDER_NAME);
    }

    @Override
    public ProviderResponse<List<NewsItem>> getNews(String symbol) {
        Optional<ProviderStockDetails> details = resolve(symbol);
        if (details.isEmpty()) {
            return ProviderResponse.failure("Unknown or untracked symbol: " + symbol, PROVIDER_NAME);
        }
        String instrumentKey = details.get().instrumentKey();
        JsonNode data = apiClient.getData("GET_NEWS",
                "/v2/news?category=instrument_keys&instrument_keys=" + encodeInstrumentKey(instrumentKey));
        JsonNode items = data.path(instrumentKey);
        List<NewsItem> news = new ArrayList<>();
        for (JsonNode item : items) {
            Instant publishedAt = item.hasNonNull("published_time") ? Instant.ofEpochMilli(item.get("published_time").asLong()) : null;
            news.add(new NewsItem(item.path("heading").asText(null), item.path("summary").asText(null),
                    item.path("article_link").asText(null), publishedAt));
        }
        return ProviderResponse.ok(news, PROVIDER_NAME);
    }

    @Override
    public ProviderResponse<List<ProviderStockDetails>> syncInstruments() {
        return ProviderResponse.ok(List.copyOf(refreshTrackedInstruments().values()), PROVIDER_NAME);
    }

    private ProviderResponse<List<MarketQuote>> topMovers(Comparator<MarketQuote> ordering) {
        Map<String, ProviderStockDetails> tracked = trackedInstruments();
        if (tracked.isEmpty()) {
            return ProviderResponse.failure("No tracked instruments available", PROVIDER_NAME);
        }
        String joined = String.join(",", tracked.values().stream().map(d -> encodeInstrumentKey(d.instrumentKey())).toList());
        JsonNode data = apiClient.getData("GET_MOVERS", "/v3/market-quote/quotes?instrument_key=" + joined);
        Map<String, ProviderStockDetails> bySymbol = new ConcurrentHashMap<>();
        tracked.values().forEach(d -> bySymbol.put(d.symbol(), d));

        List<MarketQuote> quotes = new ArrayList<>();
        Iterator<JsonNode> values = data.elements();
        while (values.hasNext()) {
            JsonNode entry = values.next();
            String symbol = entry.path("symbol").asText(null);
            ProviderStockDetails details = symbol != null ? bySymbol.get(symbol) : null;
            if (details == null) {
                continue;
            }
            quotes.add(toMarketQuote(details, entry));
        }
        List<MarketQuote> top = quotes.stream().sorted(ordering).limit(TOP_MOVERS_LIMIT).toList();
        return ProviderResponse.ok(top, PROVIDER_NAME);
    }

    private Optional<ProviderStockDetails> resolve(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            return Optional.empty();
        }
        String upper = symbol.strip().toUpperCase(Locale.ROOT);
        Map<String, ProviderStockDetails> tracked = trackedInstruments();
        if (tracked.containsKey(upper)) {
            return Optional.of(tracked.get(upper));
        }
        // Fall back to a live, one-off lookup for symbols outside the tracked/synced universe.
        try {
            String encoded = java.net.URLEncoder.encode(upper, java.nio.charset.StandardCharsets.UTF_8);
            JsonNode data = apiClient.getData("SEARCH_INSTRUMENTS",
                    "/v2/instruments/search?query=" + encoded + "&exchanges=NSE&segments=EQ&records=5");
            for (JsonNode item : data) {
                if (upper.equalsIgnoreCase(item.path("trading_symbol").asText(""))) {
                    return Optional.of(toStockDetails(item));
                }
            }
        } catch (StockProviderException ex) {
            log.warning(() -> "provider=UPSTOX operation=RESOLVE_SYMBOL symbol=" + upper + " status=FAILED errorCode=" + ex.getCategory());
        }
        return Optional.empty();
    }

    private Map<String, ProviderStockDetails> trackedInstruments() {
        if (trackedInstruments.isEmpty() || Instant.now().isAfter(trackedCacheRefreshedAt.plus(properties.getCacheTtlMinutes(), ChronoUnit.MINUTES))) {
            return refreshTrackedInstruments();
        }
        return trackedInstruments;
    }

    private synchronized Map<String, ProviderStockDetails> refreshTrackedInstruments() {
        for (String symbol : properties.getTrackedSymbols()) {
            try {
                String encoded = java.net.URLEncoder.encode(symbol, java.nio.charset.StandardCharsets.UTF_8);
                JsonNode data = apiClient.getData("SYNC_INSTRUMENT",
                        "/v2/instruments/search?query=" + encoded + "&exchanges=NSE&segments=EQ&records=5");
                for (JsonNode item : data) {
                    if (symbol.equalsIgnoreCase(item.path("trading_symbol").asText(""))) {
                        ProviderStockDetails details = toStockDetails(item);
                        trackedInstruments.put(details.symbol().toUpperCase(Locale.ROOT), details);
                        break;
                    }
                }
            } catch (StockProviderException ex) {
                log.warning(() -> "provider=UPSTOX operation=SYNC_INSTRUMENT symbol=" + symbol + " status=FAILED errorCode=" + ex.getCategory());
            }
        }
        trackedCacheRefreshedAt = Instant.now();
        return trackedInstruments;
    }

    private ProviderStockDetails toStockDetails(JsonNode item) {
        return new ProviderStockDetails(
                item.path("trading_symbol").asText(null),
                item.path("exchange").asText("NSE"),
                item.path("instrument_key").asText(null),
                item.path("isin").asText(null),
                item.path("name").asText(null),
                null, // sector - not provided by Instrument Search; requires Fundamentals API (documented limitation)
                item.path("instrument_type").asText(null),
                item.hasNonNull("lot_size") ? item.get("lot_size").asInt() : null);
    }

    private MarketQuote toMarketQuote(ProviderStockDetails details, JsonNode entry) {
        BigDecimal lastPrice = decimalOrNull(entry, "last_price");
        BigDecimal prevClose = decimalOrNull(entry, "prev_close_price");
        BigDecimal change = decimalOrNull(entry, "net_change");
        BigDecimal changePercent = percent(change, prevClose);
        Instant timestamp = parseTimestamp(entry);
        JsonNode ohlc = entry.path("ohlc");
        DataFreshness freshness = freshnessFor(timestamp);
        return new MarketQuote(
                details.symbol(), details.exchange(), details.instrumentKey(), lastPrice, prevClose,
                decimalOrNull(ohlc, "open"), decimalOrNull(ohlc, "high"), decimalOrNull(ohlc, "low"),
                entry.hasNonNull("volume") ? entry.get("volume").asLong() : null, change, changePercent,
                timestamp, null, PROVIDER_NAME, DataType.REAL_TIME, freshness);
    }

    private DataFreshness freshnessFor(Instant timestamp) {
        if (timestamp == null) {
            return DataFreshness.UNKNOWN;
        }
        long ageMinutes = ChronoUnit.MINUTES.between(timestamp, Instant.now());
        if (ageMinutes < 0) {
            return DataFreshness.UNKNOWN;
        }
        if (ageMinutes <= 5) {
            return DataFreshness.LIVE;
        }
        if (ageMinutes <= 24 * 60) {
            return DataFreshness.FRESH;
        }
        return DataFreshness.STALE;
    }

    private List<MarketCandle> parseCandles(JsonNode data) {
        List<MarketCandle> candles = new ArrayList<>();
        JsonNode rows = data.path("candles");
        for (JsonNode row : rows) {
            if (!row.isArray() || row.size() < 6) {
                continue;
            }
            Instant timestamp = OffsetDateTime.parse(row.get(0).asText()).toInstant();
            candles.add(new MarketCandle(timestamp, row.get(1).decimalValue(), row.get(2).decimalValue(),
                    row.get(3).decimalValue(), row.get(4).decimalValue(), row.get(5).asLong()));
        }
        candles.sort(Comparator.comparing(MarketCandle::timestamp));
        return candles;
    }

    private JsonNode firstValue(JsonNode data) {
        Iterator<JsonNode> it = data.elements();
        return it.hasNext() ? it.next() : null;
    }

    private Instant parseTimestamp(JsonNode entry) {
        String ts = entry.path("timestamp").asText(null);
        if (ts == null) {
            return null;
        }
        try {
            return OffsetDateTime.parse(ts).toInstant();
        } catch (Exception ex) {
            return null;
        }
    }

    private BigDecimal decimalOrNull(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isNumber() ? value.decimalValue() : null;
    }

    private BigDecimal percent(BigDecimal change, BigDecimal prevClose) {
        if (change == null || prevClose == null || prevClose.signum() == 0) {
            return null;
        }
        return change.divide(prevClose, 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
    }

    private String encodeInstrumentKey(String instrumentKey) {
        return instrumentKey.replace("|", "%7C");
    }
}
