package finadvisor.stock.provider;

import finadvisor.marketdata.ErrorCategory;

import java.time.LocalDate;
import java.util.List;

/**
 * Abstraction over an external stock market-data source (Part 4/5). The application depends only on this
 * interface - never on a specific vendor - mirroring {@link finadvisor.mutualfund.provider.MutualFundDataProvider}.
 * A new provider can be plugged in later by adding an implementation and pointing {@code stock.provider.name}
 * at it. Implementations must:
 * <ul>
 *   <li>never throw for expected "not found" results (wrap in {@link ProviderResponse#failure})</li>
 *   <li>only throw {@link StockProviderException} for transport/parsing failures</li>
 *   <li>never expose provider-specific response objects to controllers - only the records in this package</li>
 *   <li>never mark data as {@code REAL_TIME}/{@code LIVE} unless the underlying call actually returned
 *       real-time data for that request and entitlement</li>
 * </ul>
 */
public interface StockMarketDataProvider {

    String getProviderName();

    ProviderResponse<List<ProviderStockDetails>> searchStocks(String query);

    ProviderResponse<ProviderStockDetails> getStockDetails(String symbol);

    ProviderResponse<MarketQuote> getCurrentPrice(String symbol);

    ProviderResponse<List<MarketCandle>> getHistoricalPrices(String symbol, LocalDate from, LocalDate to);

    ProviderResponse<List<MarketCandle>> getIntradayPrices(String symbol);

    ProviderResponse<List<MarketIndex>> getMarketIndices();

    ProviderResponse<List<MarketQuote>> getTopGainers();

    ProviderResponse<List<MarketQuote>> getTopLosers();

    ProviderResponse<List<MarketQuote>> getMostActive();

    ProviderResponse<MarketStatusInfo> getMarketStatus(String exchange);

    ProviderResponse<List<NewsItem>> getNews(String symbol);

    /** Full tracked instrument catalog for the sync job; analogous to {@code MutualFundDataProvider.syncFunds()}. */
    ProviderResponse<List<ProviderStockDetails>> syncInstruments();

    // --- Optional real-time streaming (Part 5/10) - default to "not supported" so providers that only
    // offer REST/polling access do not need to implement these. See docs/financial-data-providers.md for
    // the WebSocket streaming roadmap. ---

    default void connectMarketStream() {
        throw unsupported("connectMarketStream");
    }

    default void subscribe(List<String> symbols) {
        throw unsupported("subscribe");
    }

    default void unsubscribe(List<String> symbols) {
        throw unsupported("unsubscribe");
    }

    default void disconnect() {
        throw unsupported("disconnect");
    }

    private StockProviderException unsupported(String operation) {
        return new StockProviderException(
                getProviderName() + " does not implement real-time streaming (" + operation + ") in this deployment",
                ErrorCategory.UNSUPPORTED_OPERATION);
    }
}
