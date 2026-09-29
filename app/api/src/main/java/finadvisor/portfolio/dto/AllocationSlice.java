package finadvisor.portfolio.dto;

import java.math.BigDecimal;

public record AllocationSlice(
        String label,
        BigDecimal marketValue,
        BigDecimal percentage,
        int holdingsCount
) {
}
