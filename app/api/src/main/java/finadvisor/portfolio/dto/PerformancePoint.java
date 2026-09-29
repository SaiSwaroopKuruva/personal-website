package finadvisor.portfolio.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PerformancePoint(
        LocalDate date,
        BigDecimal investedValue,
        BigDecimal marketValue,
        BigDecimal gainLoss,
        boolean complete
) {
}
