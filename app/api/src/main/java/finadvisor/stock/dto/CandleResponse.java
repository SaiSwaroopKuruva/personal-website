package finadvisor.stock.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record CandleResponse(Instant timestamp, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close, Long volume) {
}
