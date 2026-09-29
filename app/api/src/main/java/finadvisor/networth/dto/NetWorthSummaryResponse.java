package finadvisor.networth.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Net worth = total assets - total liabilities (Part 15). {@code investmentAssets} come from live portfolio
 * valuations (may be partial); {@code nonInvestmentAssets}/{@code totalLiabilities} come from manually
 * entered, dated values. {@code disclosure} always explains this is a tracked - not complete - balance sheet.
 */
public record NetWorthSummaryResponse(
        BigDecimal totalAssets,
        BigDecimal totalLiabilities,
        BigDecimal netWorth,
        BigDecimal investmentAssets,
        BigDecimal nonInvestmentAssets,
        boolean partialInvestmentValuation,
        Instant asOf,
        String disclosure
) {
}
