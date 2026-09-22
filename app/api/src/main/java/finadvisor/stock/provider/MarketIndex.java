package finadvisor.stock.provider;

import java.math.BigDecimal;
import java.time.Instant;

public record MarketIndex(String name, String instrumentKey, BigDecimal lastPrice, BigDecimal change, BigDecimal changePercent, Instant timestamp) {
}
