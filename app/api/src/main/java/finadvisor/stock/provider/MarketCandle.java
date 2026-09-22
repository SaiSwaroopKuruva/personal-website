package finadvisor.stock.provider;

import java.math.BigDecimal;
import java.time.Instant;

/** A single OHLC candle, from either the historical or intraday candle endpoints. */
public record MarketCandle(Instant timestamp, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close, Long volume) {
}
