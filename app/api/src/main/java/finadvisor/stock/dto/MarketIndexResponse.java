package finadvisor.stock.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record MarketIndexResponse(String name, BigDecimal lastPrice, BigDecimal change, BigDecimal changePercent, Instant timestamp) {
}
