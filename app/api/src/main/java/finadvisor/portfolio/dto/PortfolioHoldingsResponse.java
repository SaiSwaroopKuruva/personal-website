package finadvisor.portfolio.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record PortfolioHoldingsResponse(
        List<HoldingResponse> holdings,
        BigDecimal totalCostBasis,
        BigDecimal totalMarketValue,
        boolean partialValuation,
        String partialValuationMessage,
        Instant asOf
) {
}
