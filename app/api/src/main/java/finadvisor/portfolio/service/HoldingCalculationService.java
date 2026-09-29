package finadvisor.portfolio.service;

import finadvisor.portfolio.entity.PortfolioTransaction;

import java.util.List;
import java.util.UUID;

public interface HoldingCalculationService {

    /** Derives current holding positions (FIFO cost basis) for every asset in the portfolio's ledger. */
    List<HoldingPosition> calculatePositions(List<PortfolioTransaction> transactions);

    /** Total realized gain/loss across all assets in the portfolio's ledger. */
    java.math.BigDecimal totalRealizedGain(List<PortfolioTransaction> transactions);

    /** Builds the signed, dated investor cash-flow series used by {@link XirrService} (Part 12). */
    List<CashFlowEntry> buildInvestorCashFlows(List<PortfolioTransaction> transactions);

    /** Validates a transaction against portfolio {@code portfolioId}'s existing ledger (e.g. no oversell). */
    void validateNewTransaction(UUID portfolioId, PortfolioTransaction candidate, List<PortfolioTransaction> existingLedger);
}
