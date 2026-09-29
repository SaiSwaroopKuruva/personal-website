package finadvisor.portfolio.service.impl;

import finadvisor.portfolio.entity.Portfolio;
import finadvisor.portfolio.entity.PortfolioTransaction;
import finadvisor.portfolio.entity.PortfolioValuationSnapshot;
import finadvisor.portfolio.repository.PortfolioRepository;
import finadvisor.portfolio.repository.PortfolioTransactionRepository;
import finadvisor.portfolio.repository.PortfolioValuationSnapshotRepository;
import finadvisor.portfolio.service.HoldingCalculationService;
import finadvisor.portfolio.service.HoldingPosition;
import finadvisor.portfolio.service.PortfolioSnapshotService;
import finadvisor.portfolio.service.PortfolioValuationService;
import finadvisor.portfolio.service.ValuedHolding;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Snapshot capture (Part 16). Idempotent per (portfolio, date): captureSnapshot looks up any existing row
 * for that date first and replaces it, so a re-run (e.g. after a Render restart or manual re-trigger)
 * never creates a duplicate - the DB unique constraint on (portfolio_id, snapshot_date) is a backstop.
 */
@Service
@Transactional
public class PortfolioSnapshotServiceImpl implements PortfolioSnapshotService {

    private static final Logger log = Logger.getLogger(PortfolioSnapshotServiceImpl.class.getName());

    private final PortfolioValuationSnapshotRepository snapshotRepository;
    private final PortfolioRepository portfolioRepository;
    private final PortfolioTransactionRepository transactionRepository;
    private final HoldingCalculationService holdingCalculationService;
    private final PortfolioValuationService valuationService;

    public PortfolioSnapshotServiceImpl(PortfolioValuationSnapshotRepository snapshotRepository, PortfolioRepository portfolioRepository,
                                         PortfolioTransactionRepository transactionRepository, HoldingCalculationService holdingCalculationService,
                                         PortfolioValuationService valuationService) {
        this.snapshotRepository = snapshotRepository;
        this.portfolioRepository = portfolioRepository;
        this.transactionRepository = transactionRepository;
        this.holdingCalculationService = holdingCalculationService;
        this.valuationService = valuationService;
    }

    @Override
    public PortfolioValuationSnapshot captureSnapshot(UUID portfolioId, LocalDate date) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("Portfolio not found: " + portfolioId));
        List<PortfolioTransaction> ledger = transactionRepository.findByPortfolioIdOrderByTransactionDateAscCreatedAtAsc(portfolioId);
        List<PortfolioTransaction> upToDate = ledger.stream().filter(tx -> !tx.getTransactionDate().isAfter(date)).toList();

        List<HoldingPosition> positions = holdingCalculationService.calculatePositions(upToDate);
        List<ValuedHolding> valued = valuationService.valueHoldings(positions);
        BigDecimal realizedGain = holdingCalculationService.totalRealizedGain(upToDate);

        BigDecimal investedValue = positions.stream().map(HoldingPosition::costBasis).reduce(BigDecimal.ZERO, BigDecimal::add);
        boolean complete = valued.stream().allMatch(v -> v.marketValue() != null) || valued.isEmpty();
        BigDecimal marketValue = valued.stream()
                .filter(v -> v.marketValue() != null)
                .map(ValuedHolding::marketValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal unrealizedGain = valued.stream()
                .filter(v -> v.unrealizedGain() != null)
                .map(ValuedHolding::unrealizedGain)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        PortfolioValuationSnapshot snapshot = snapshotRepository.findByPortfolioIdAndSnapshotDate(portfolioId, date)
                .orElseGet(() -> PortfolioValuationSnapshot.builder().portfolio(portfolio).snapshotDate(date).build());
        snapshot.setInvestedValue(investedValue.setScale(2, RoundingMode.HALF_UP));
        snapshot.setMarketValue(marketValue.setScale(2, RoundingMode.HALF_UP));
        snapshot.setRealizedGain(realizedGain);
        snapshot.setUnrealizedGain(unrealizedGain.setScale(2, RoundingMode.HALF_UP));
        snapshot.setComplete(complete);
        snapshot.setCalculatedAt(java.time.Instant.now());
        return snapshotRepository.save(snapshot);
    }

    @Override
    public void invalidateFrom(UUID portfolioId, LocalDate fromDate) {
        snapshotRepository.deleteByPortfolioIdAndSnapshotDateGreaterThanEqual(portfolioId, fromDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PortfolioValuationSnapshot> getSnapshots(UUID portfolioId, LocalDate from, LocalDate to) {
        return snapshotRepository.findByPortfolioIdAndSnapshotDateBetweenOrderBySnapshotDateAsc(portfolioId, from, to);
    }

    @Override
    public void captureAllPortfolios() {
        LocalDate today = LocalDate.now();
        List<Portfolio> portfolios = portfolioRepository.findAll().stream().filter(p -> p.getArchivedAt() == null).toList();
        for (Portfolio portfolio : portfolios) {
            try {
                captureSnapshot(portfolio.getId(), today);
            } catch (RuntimeException ex) {
                // One portfolio's provider failure must not abort the batch for every other user (Part 16).
                log.log(Level.WARNING, "Failed to capture snapshot for portfolio " + portfolio.getId(), ex);
            }
        }
    }
}
