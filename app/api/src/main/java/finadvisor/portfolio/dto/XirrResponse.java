package finadvisor.portfolio.dto;

import java.math.BigDecimal;
import java.time.Instant;

/** Structured XIRR result (Part 12) - never a bare percentage; {@code status} explains why it may be absent. */
public record XirrResponse(
        BigDecimal xirrPercent,
        XirrStatus status,
        String message,
        Instant calculatedAt
) {
}
