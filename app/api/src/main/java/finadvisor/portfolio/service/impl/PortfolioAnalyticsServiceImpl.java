package finadvisor.portfolio.service.impl;

import finadvisor.portfolio.dto.AllocationResponse;
import finadvisor.portfolio.dto.AllocationSlice;
import finadvisor.portfolio.dto.HoldingResponse;
import finadvisor.portfolio.dto.PerformancePoint;
import finadvisor.portfolio.dto.PerformanceResponse;
import finadvisor.portfolio.dto.PortfolioHoldingsResponse;
import finadvisor.portfolio.dto.PortfolioSummaryResponse;
import finadvisor.portfolio.dto.XirrResponse;
import finadvisor.portfolio.entity.AssetType;
import finadvisor.portfolio.entity.Portfolio;
import finadvisor.portfolio.entity.PortfolioTransaction;
import finadvisor.portfolio.entity.PortfolioValuationSnapshot;
import finadvisor.portfolio.repository.PortfolioValuationSnapshotRepository;
import finadvisor.portfolio.service.CashFlowEntry;
import finadvisor.portfolio.service.HoldingCalculationService;
import finadvisor.portfolio.service.HoldingPosition;
import finadvisor.portfolio.service.PortfolioAnalyticsService;
import finadvisor.portfolio.service.PortfolioService;
import finadvisor.portfolio.service.PortfolioTransactionService;
import finadvisor.portfolio.service.PortfolioValuationService;
import finadvisor.portfolio.service.ValuedHolding;
import finadvisor.portfolio.service.XirrService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PortfolioAnalyticsServiceImpl implements PortfolioAnalyticsService {

    private static final String PARTIAL_MESSAGE =
            "Some holdings could not be valued because market data is currently unavailable; totals below exclude them and may understate the portfolio.";

    private final PortfolioService portfolioService;
    private final PortfolioTransactionService transactionService;
    private final HoldingCalculationService holdingCalculationService;
    private final PortfolioValuationService valuationService;
    private final XirrService xirrService;
    private final PortfolioValuationSnapshotRepository snapshotRepository;

    public PortfolioAnalyticsServiceImpl(PortfolioService portfolioService, PortfolioTransactionService transactionService,
                                          HoldingCalculationService holdingCalculationService, PortfolioValuationService valuationService,
                                          XirrService xirrService, PortfolioValuationSnapshotRepository snapshotRepository) {
        this.portfolioService = portfolioService;
        this.transactionService = transactionService;
        this.holdingCalculationService = holdingCalculationService;
        this.valuationService = valuationService;
        this.xirrService = xirrService;
        this.snapshotRepository = snapshotRepository;
    }

    @Override
    public PortfolioHoldingsResponse getHoldings(String userEmail, UUID portfolioId) {
        Portfolio portfolio = portfolioService.requireOwnedPortfolio(userEmail, portfolioId);
        List<ValuedHolding> valued = loadValuedHoldings(portfolio.getId());

        BigDecimal totalCostBasis = valued.stream().map(v -> v.position().costBasis()).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalMarketValue = valued.stream().filter(v -> v.marketValue() != null).map(ValuedHolding::marketValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        boolean partial = valued.stream().anyMatch(v -> v.marketValue() == null);

        List<HoldingResponse> holdings = valued.stream()
                .map(v -> toHoldingResponse(v, totalMarketValue))
                .toList();

        return new PortfolioHoldingsResponse(holdings, totalCostBasis.setScale(2, RoundingMode.HALF_UP),
                totalMarketValue.setScale(2, RoundingMode.HALF_UP), partial, partial ? PARTIAL_MESSAGE : null, Instant.now());
    }

    @Override
    public PortfolioSummaryResponse getSummary(String userEmail, UUID portfolioId) {
        Portfolio portfolio = portfolioService.requireOwnedPortfolio(userEmail, portfolioId);
        List<PortfolioTransaction> ledger = transactionService.loadLedger(portfolio.getId());
        List<ValuedHolding> valued = valuationService.valueHoldings(holdingCalculationService.calculatePositions(ledger));

        BigDecimal investedAmount = valued.stream().map(v -> v.position().costBasis()).reduce(BigDecimal.ZERO, BigDecimal::add);
        boolean partial = valued.stream().anyMatch(v -> v.marketValue() == null);
        BigDecimal currentMarketValue = partial && valued.stream().allMatch(v -> v.marketValue() == null) && !valued.isEmpty()
                ? null
                : valued.stream().filter(v -> v.marketValue() != null).map(ValuedHolding::marketValue).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal realizedGain = holdingCalculationService.totalRealizedGain(ledger);
        BigDecimal unrealizedGain = valued.stream().filter(v -> v.unrealizedGain() != null).map(ValuedHolding::unrealizedGain)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        // Total/absolute gain-loss (Part 13) = realized + unrealized; defined even when market value is partial.
        BigDecimal totalGainLoss = realizedGain.add(unrealizedGain);
        BigDecimal percentGainLoss = investedAmount.signum() > 0
                ? totalGainLoss.divide(investedAmount, 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)
                : null;

        BigDecimal stockValue = sumMarketValueByType(valued, AssetType.STOCK);
        BigDecimal mfValue = sumMarketValueByType(valued, AssetType.MUTUAL_FUND);
        BigDecimal totalValued = stockValue.add(mfValue);
        BigDecimal stockPct = totalValued.signum() > 0 ? stockValue.divide(totalValued, 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP) : null;
        BigDecimal mfPct = totalValued.signum() > 0 ? mfValue.divide(totalValued, 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP) : null;

        Instant lastValuationAt = valued.stream().map(v -> v.valuation().retrievedAt()).filter(java.util.Objects::nonNull)
                .max(Comparator.naturalOrder()).orElse(null);

        XirrResponse xirr = computeXirr(ledger, valued);

        return new PortfolioSummaryResponse(
                portfolio.getId(), portfolio.getName(), investedAmount.setScale(2, RoundingMode.HALF_UP),
                currentMarketValue, totalGainLoss.setScale(2, RoundingMode.HALF_UP), percentGainLoss,
                realizedGain, unrealizedGain, valued.size(), stockPct, mfPct, lastValuationAt, partial,
                partial ? PARTIAL_MESSAGE : null, xirr);
    }

    @Override
    public AllocationResponse getAllocation(String userEmail, UUID portfolioId) {
        Portfolio portfolio = portfolioService.requireOwnedPortfolio(userEmail, portfolioId);
        List<ValuedHolding> valued = loadValuedHoldings(portfolio.getId());
        BigDecimal totalMarketValue = valued.stream().filter(v -> v.marketValue() != null).map(ValuedHolding::marketValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        boolean partial = valued.stream().anyMatch(v -> v.marketValue() == null);

        Map<String, List<ValuedHolding>> byClass = new LinkedHashMap<>();
        for (ValuedHolding v : valued) {
            if (v.marketValue() == null) continue;
            byClass.computeIfAbsent(v.position().assetType() == AssetType.STOCK ? "STOCK" : "MUTUAL_FUND", k -> new ArrayList<>()).add(v);
        }
        List<AllocationSlice> byAssetClass = byClass.entrySet().stream()
                .map(e -> toSlice(e.getKey(), e.getValue(), totalMarketValue))
                .toList();

        List<AllocationSlice> byHolding = valued.stream()
                .filter(v -> v.marketValue() != null)
                .map(v -> toSlice(v.name() != null ? v.name() : v.position().symbol(), List.of(v), totalMarketValue))
                .toList();

        return new AllocationResponse(byAssetClass, byHolding, totalMarketValue.setScale(2, RoundingMode.HALF_UP),
                partial, partial ? PARTIAL_MESSAGE : null);
    }

    @Override
    public PerformanceResponse getPerformance(String userEmail, UUID portfolioId, String range) {
        Portfolio portfolio = portfolioService.requireOwnedPortfolio(userEmail, portfolioId);
        List<PortfolioValuationSnapshot> all = snapshotRepository.findByPortfolioIdOrderBySnapshotDateAsc(portfolio.getId());
        if (all.isEmpty()) {
            return new PerformanceResponse(range, List.of(), true, null,
                    "No valuation history is available yet. Snapshots build up daily as the portfolio is tracked.");
        }

        LocalDate earliest = all.get(0).getSnapshotDate();
        LocalDate today = LocalDate.now();
        LocalDate requestedFrom = resolveRangeStart(range, today);
        boolean limited = requestedFrom != null && requestedFrom.isBefore(earliest);
        LocalDate effectiveFrom = requestedFrom == null || limited ? earliest : requestedFrom;

        List<PerformancePoint> points = all.stream()
                .filter(s -> !s.getSnapshotDate().isBefore(effectiveFrom))
                .map(s -> new PerformancePoint(s.getSnapshotDate(), s.getInvestedValue(), s.getMarketValue(),
                        s.getMarketValue().subtract(s.getInvestedValue()).add(s.getRealizedGain()), s.isComplete()))
                .toList();

        String limitationMessage = limited
                ? "Only " + earliest + " to " + today + " of history is available; the requested range could not be fully reconstructed."
                : null;

        return new PerformanceResponse(range, points, limited, earliest, limitationMessage);
    }

    @Override
    public XirrResponse getXirr(String userEmail, UUID portfolioId) {
        Portfolio portfolio = portfolioService.requireOwnedPortfolio(userEmail, portfolioId);
        List<PortfolioTransaction> ledger = transactionService.loadLedger(portfolio.getId());
        List<ValuedHolding> valued = valuationService.valueHoldings(holdingCalculationService.calculatePositions(ledger));
        return computeXirr(ledger, valued);
    }

    private XirrResponse computeXirr(List<PortfolioTransaction> ledger, List<ValuedHolding> valued) {
        List<CashFlowEntry> flows = new ArrayList<>(holdingCalculationService.buildInvestorCashFlows(ledger));
        if (!valued.isEmpty()) {
            BigDecimal terminalValue = valued.stream().filter(v -> v.marketValue() != null).map(ValuedHolding::marketValue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (terminalValue.signum() > 0) {
                flows.add(new CashFlowEntry(LocalDate.now(), terminalValue));
            }
        }
        return xirrService.calculate(flows);
    }

    private List<ValuedHolding> loadValuedHoldings(UUID portfolioId) {
        List<PortfolioTransaction> ledger = transactionService.loadLedger(portfolioId);
        return valuationService.valueHoldings(holdingCalculationService.calculatePositions(ledger));
    }

    private BigDecimal sumMarketValueByType(List<ValuedHolding> valued, AssetType type) {
        return valued.stream().filter(v -> v.position().assetType() == type && v.marketValue() != null)
                .map(ValuedHolding::marketValue).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private AllocationSlice toSlice(String label, List<ValuedHolding> group, BigDecimal totalMarketValue) {
        BigDecimal value = group.stream().map(ValuedHolding::marketValue).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal pct = totalMarketValue.signum() > 0
                ? value.divide(totalMarketValue, 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        return new AllocationSlice(label, value.setScale(2, RoundingMode.HALF_UP), pct, group.size());
    }

    private HoldingResponse toHoldingResponse(ValuedHolding v, BigDecimal totalMarketValue) {
        BigDecimal allocationPercent = v.marketValue() != null && totalMarketValue.signum() > 0
                ? v.marketValue().divide(totalMarketValue, 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)
                : null;
        return new HoldingResponse(
                v.position().assetType(), v.position().symbol(), v.position().exchange(), v.name(),
                v.position().quantity(), v.position().averageCost(), v.position().costBasis(),
                v.valuation(), v.marketValue(), v.unrealizedGain(), v.unrealizedGainPercent(), allocationPercent);
    }

    private LocalDate resolveRangeStart(String range, LocalDate today) {
        if (range == null) return null;
        return switch (range.toUpperCase()) {
            case "1M" -> today.minusMonths(1);
            case "3M" -> today.minusMonths(3);
            case "6M" -> today.minusMonths(6);
            case "1Y" -> today.minusYears(1);
            case "3Y" -> today.minusYears(3);
            case "5Y" -> today.minusYears(5);
            default -> null; // ALL, or unrecognized -> full history
        };
    }
}
