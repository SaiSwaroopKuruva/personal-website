package finadvisor.dto.calculator;

import java.math.BigDecimal;

public record SwpCalculatorResponse(
        BigDecimal initialInvestment,
        BigDecimal withdrawalPerMonth,
        BigDecimal expectedAnnualReturn,
        int durationYears,
        BigDecimal totalWithdrawn,
        BigDecimal remainingValue,
        BigDecimal estimatedGrowth,
        boolean exhausted,
        Integer exhaustedAfterMonths,
        boolean estimate,
        String disclaimer
) {
}
