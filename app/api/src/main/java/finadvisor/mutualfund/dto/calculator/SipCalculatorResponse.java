package finadvisor.mutualfund.dto.calculator;

import java.math.BigDecimal;

public record SipCalculatorResponse(
        BigDecimal monthlyInvestment,
        BigDecimal expectedAnnualReturn,
        int durationYears,
        BigDecimal totalInvestment,
        BigDecimal estimatedReturns,
        BigDecimal futureValue,
        boolean estimate,
        String disclaimer
) {
}
