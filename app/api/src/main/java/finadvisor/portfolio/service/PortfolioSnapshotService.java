package finadvisor.portfolio.service;

import finadvisor.portfolio.entity.PortfolioValuationSnapshot;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PortfolioSnapshotService {

    /** Creates (or replaces) today's snapshot for one portfolio; idempotent for a given portfolio/date (Part 16). */
    PortfolioValuationSnapshot captureSnapshot(UUID portfolioId, LocalDate date);

    /** Deletes stored snapshots from {@code fromDate} onward so they are recalculated on next capture (Part 7/16). */
    void invalidateFrom(UUID portfolioId, LocalDate fromDate);

    List<PortfolioValuationSnapshot> getSnapshots(UUID portfolioId, LocalDate from, LocalDate to);

    /** Runs the daily snapshot job across every non-archived portfolio (Part 16). */
    void captureAllPortfolios();
}
