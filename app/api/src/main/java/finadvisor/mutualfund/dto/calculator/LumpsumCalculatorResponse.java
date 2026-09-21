package finadvisor.mutualfund.dto.calculator;

import java.math.BigDecimal;

public record LumpsumCalculatorResponse(
        BigDecimal principal,
        BigDecimal expectedAnnualReturn,
        int durationYears,
        BigDecimal investedAmount,
        BigDecimal estimatedReturns,
        BigDecimal futureValue,
        boolean estimate,
        String disclaimer
) {
}
