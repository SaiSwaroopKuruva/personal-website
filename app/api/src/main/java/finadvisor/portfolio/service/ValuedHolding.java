package finadvisor.portfolio.service;

import finadvisor.portfolio.dto.ValuationInfo;

import java.math.BigDecimal;

/** A holding position combined with its live (or last-known) valuation (Part 10). */
public record ValuedHolding(
        HoldingPosition position,
        String name,
        ValuationInfo valuation,
        BigDecimal marketValue,
        BigDecimal unrealizedGain,
        BigDecimal unrealizedGainPercent
) {
}
