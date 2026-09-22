package finadvisor.stock.dto;

import finadvisor.marketdata.DataFreshness;
import finadvisor.marketdata.DataType;

import java.math.BigDecimal;
import java.time.Instant;

/** API response shape for a stock quote (Part 19) - always carries source/dataType/freshness so the frontend never presents stale data as live. */
public record StockQuoteResponse(
        String symbol,
        String exchange,
        BigDecimal lastTradedPrice,
        BigDecimal previousClose,
        BigDecimal open,
        BigDecimal high,
        BigDecimal low,
        Long volume,
        BigDecimal change,
        BigDecimal changePercent,
        Instant timestamp,
        String source,
        DataType dataType,
        DataFreshness freshness
) {
}
