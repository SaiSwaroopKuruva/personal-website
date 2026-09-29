package finadvisor.portfolio.service;

import java.util.List;

public interface PortfolioValuationService {

    /**
     * Resolves the latest price/NAV for each position via the existing Phase 3.5 stock (Upstox) and
     * mutual-fund (AMFI) data services and computes market value / unrealized gain (Part 10). Never
     * throws for an individual unresolvable holding - it is returned with {@code ValuationStatus.UNAVAILABLE}
     * instead (Part 9 - missing prices must never be silently treated as zero).
     */
    List<ValuedHolding> valueHoldings(List<HoldingPosition> positions);
}
