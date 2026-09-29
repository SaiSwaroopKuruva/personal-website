package finadvisor.networth.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record NetWorthHistoryPoint(
        LocalDate date,
        BigDecimal investmentValue,
        BigDecimal nonInvestmentAssetValue,
        BigDecimal liabilities,
        BigDecimal netWorth
) {
}
