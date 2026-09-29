package finadvisor.portfolio.dto;

import finadvisor.portfolio.entity.AssetType;

import java.math.BigDecimal;

/** One derived holding line (Part 9) - valuation fields are null (not zero) when a price/NAV is unavailable. */
public record HoldingResponse(
        AssetType assetType,
        String symbol,
        String exchange,
        String name,
        BigDecimal quantity,
        BigDecimal averageCost,
        BigDecimal costBasis,
        ValuationInfo valuation,
        BigDecimal marketValue,
        BigDecimal unrealizedGain,
        BigDecimal unrealizedGainPercent,
        BigDecimal allocationPercent
) {
}
