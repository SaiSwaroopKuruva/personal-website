package finadvisor.stock.provider;

import finadvisor.marketdata.DataFreshness;
import finadvisor.marketdata.DataType;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Canonical, provider-agnostic market quote (Part 8). Every {@link StockMarketDataProvider} implementation
 * maps its vendor-specific response into this shape before it ever reaches a service or controller -
 * provider-specific response objects must never leak past this package.
 */
public record MarketQuote(
        String symbol,
        String exchange,
        String instrumentKey,
        BigDecimal lastTradedPrice,
        BigDecimal previousClose,
        BigDecimal open,
        BigDecimal high,
        BigDecimal low,
        Long volume,
        BigDecimal change,
        BigDecimal changePercent,
        Instant timestamp,
        String marketStatus,
        String source,
        DataType dataType,
        DataFreshness freshness
) {
}
