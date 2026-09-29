package finadvisor.portfolio.service;

import finadvisor.portfolio.entity.AssetType;

import java.math.BigDecimal;

/**
 * A derived holding position for one asset within a portfolio, computed from the transaction ledger
 * (Part 5). {@code costBasis}/{@code averageCost} reflect only currently-held units (FIFO, Part 5).
 */
public record HoldingPosition(
        AssetType assetType,
        String symbol,
        String exchange,
        BigDecimal quantity,
        BigDecimal costBasis,
        BigDecimal averageCost,
        BigDecimal realizedGain,
        BigDecimal dividendIncome,
        BigDecimal totalFees,
        BigDecimal totalTaxes
) {
}
