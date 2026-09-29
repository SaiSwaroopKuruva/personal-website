package finadvisor.portfolio.dto;

import java.math.BigDecimal;
import java.time.Instant;

/** Provider-backed price/NAV with explicit provenance and freshness (Part 10) - distinguishes source timestamp from retrieval timestamp. */
public record ValuationInfo(
        BigDecimal price,
        String source,
        Instant sourceTimestamp,
        Instant retrievedAt,
        ValuationStatus status
) {
    public static ValuationInfo unavailable() {
        return new ValuationInfo(null, null, null, Instant.now(), ValuationStatus.UNAVAILABLE);
    }
}
