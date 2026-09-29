package finadvisor.portfolio.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Portfolio overview metrics (Part 8). {@code currentMarketValue}/{@code unrealizedGainLoss} are null,
 * not zero, when one or more holdings could not be valued - see {@code partialValuation}.
 */
public record PortfolioSummaryResponse(
        UUID portfolioId,
        String portfolioName,
        BigDecimal investedAmount,
        BigDecimal currentMarketValue,
        BigDecimal absoluteGainLoss,
        BigDecimal percentGainLoss,
        BigDecimal realizedGainLoss,
        BigDecimal unrealizedGainLoss,
        int holdingsCount,
        BigDecimal stockAllocationPercent,
        BigDecimal mutualFundAllocationPercent,
        Instant lastValuationAt,
        boolean partialValuation,
        String partialValuationMessage,
        XirrResponse xirr
) {
}
