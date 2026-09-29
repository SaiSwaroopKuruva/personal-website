package finadvisor.portfolio.dto;

import java.math.BigDecimal;
import java.util.List;

public record AllocationResponse(
        List<AllocationSlice> byAssetClass,
        List<AllocationSlice> byHolding,
        BigDecimal totalMarketValue,
        boolean partialValuation,
        String partialValuationMessage
) {
}
