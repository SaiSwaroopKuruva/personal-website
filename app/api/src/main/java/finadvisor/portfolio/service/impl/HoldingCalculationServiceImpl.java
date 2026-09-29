package finadvisor.portfolio.service.impl;

import finadvisor.portfolio.entity.AssetType;
import finadvisor.portfolio.entity.PortfolioTransaction;
import finadvisor.portfolio.entity.TransactionType;
import finadvisor.portfolio.exception.InsufficientUnitsException;
import finadvisor.portfolio.service.CashFlowEntry;
import finadvisor.portfolio.service.HoldingCalculationService;
import finadvisor.portfolio.service.HoldingPosition;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * FIFO cost-basis engine (Part 5). This is the single place that walks the transaction ledger, so
 * {@link finadvisor.portfolio.service.PortfolioValuationService}, XIRR cash-flow building and oversell
 * validation all share one deterministic, testable implementation.
 */
@Service
public class HoldingCalculationServiceImpl implements HoldingCalculationService {

    private static final MathContext MC = new MathContext(20, RoundingMode.HALF_UP);
    private static final Set<TransactionType> BUY_TYPES = Set.of(TransactionType.BUY, TransactionType.PURCHASE);
    private static final Set<TransactionType> SELL_TYPES = Set.of(TransactionType.SELL, TransactionType.REDEMPTION);

    private record Lot(BigDecimal quantity, BigDecimal unitCost) {
    }

    private static final class AssetLedgerState {
        final Deque<Lot> lots = new ArrayDeque<>();
        BigDecimal realizedGain = BigDecimal.ZERO;
        BigDecimal dividendIncome = BigDecimal.ZERO;
        BigDecimal totalFees = BigDecimal.ZERO;
        BigDecimal totalTaxes = BigDecimal.ZERO;
    }

    @Override
    public List<HoldingPosition> calculatePositions(List<PortfolioTransaction> transactions) {
        Map<String, AssetLedgerState> states = new LinkedHashMap<>();
        Map<String, PortfolioTransaction> firstSeen = new LinkedHashMap<>();

        for (PortfolioTransaction tx : sorted(transactions)) {
            String key = assetKey(tx);
            firstSeen.putIfAbsent(key, tx);
            AssetLedgerState state = states.computeIfAbsent(key, k -> new AssetLedgerState());
            apply(tx, state);
        }

        List<HoldingPosition> positions = new ArrayList<>();
        for (Map.Entry<String, AssetLedgerState> entry : states.entrySet()) {
            AssetLedgerState state = entry.getValue();
            BigDecimal quantity = state.lots.stream().map(Lot::quantity).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal costBasis = state.lots.stream()
                    .map(l -> l.quantity().multiply(l.unitCost(), MC))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal averageCost = quantity.signum() > 0 ? costBasis.divide(quantity, MC) : BigDecimal.ZERO;
            PortfolioTransaction sample = firstSeen.get(entry.getKey());
            positions.add(new HoldingPosition(
                    sample.getAssetType(),
                    sample.getAssetType() == AssetType.STOCK ? sample.getStockSymbol() : sample.getMutualFundSchemeCode(),
                    sample.getStockExchange(),
                    quantity.setScale(6, RoundingMode.HALF_UP),
                    costBasis.setScale(2, RoundingMode.HALF_UP),
                    averageCost.setScale(4, RoundingMode.HALF_UP),
                    state.realizedGain.setScale(2, RoundingMode.HALF_UP),
                    state.dividendIncome.setScale(2, RoundingMode.HALF_UP),
                    state.totalFees.setScale(2, RoundingMode.HALF_UP),
                    state.totalTaxes.setScale(2, RoundingMode.HALF_UP)
            ));
        }
        // Only report positions with a currently non-zero quantity or historical activity worth surfacing.
        return positions.stream().filter(p -> p.quantity().signum() != 0).toList();
    }

    @Override
    public BigDecimal totalRealizedGain(List<PortfolioTransaction> transactions) {
        Map<String, AssetLedgerState> states = new LinkedHashMap<>();
        for (PortfolioTransaction tx : sorted(transactions)) {
            AssetLedgerState state = states.computeIfAbsent(assetKey(tx), k -> new AssetLedgerState());
            apply(tx, state);
        }
        return states.values().stream().map(s -> s.realizedGain).reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public List<CashFlowEntry> buildInvestorCashFlows(List<PortfolioTransaction> transactions) {
        List<CashFlowEntry> flows = new ArrayList<>();
        for (PortfolioTransaction tx : sorted(transactions)) {
            switch (tx.getTransactionType()) {
                case BUY, PURCHASE, FEE, TAX -> flows.add(new CashFlowEntry(tx.getTransactionDate(), tx.getNetAmount().negate()));
                case SELL, REDEMPTION, DIVIDEND -> flows.add(new CashFlowEntry(tx.getTransactionDate(), tx.getNetAmount()));
                case ADJUSTMENT -> { /* non-cash by definition (Part 24) - never enters the XIRR cash-flow series */ }
            }
        }
        return flows;
    }

    @Override
    public void validateNewTransaction(UUID portfolioId, PortfolioTransaction candidate, List<PortfolioTransaction> existingLedger) {
        List<PortfolioTransaction> merged = new ArrayList<>(existingLedger);
        merged.add(candidate);
        // Re-running full FIFO surfaces InsufficientUnitsException exactly as it would occur chronologically,
        // including when a transaction is backdated ahead of/behind other entries.
        calculatePositionsAllowingZero(merged);
    }

    private void calculatePositionsAllowingZero(List<PortfolioTransaction> transactions) {
        Map<String, AssetLedgerState> states = new LinkedHashMap<>();
        for (PortfolioTransaction tx : sorted(transactions)) {
            AssetLedgerState state = states.computeIfAbsent(assetKey(tx), k -> new AssetLedgerState());
            apply(tx, state);
        }
    }

    private void apply(PortfolioTransaction tx, AssetLedgerState state) {
        TransactionType type = tx.getTransactionType();
        BigDecimal quantity = tx.getQuantity() != null ? tx.getQuantity() : BigDecimal.ZERO;
        BigDecimal fees = tx.getFees() != null ? tx.getFees() : BigDecimal.ZERO;
        BigDecimal taxes = tx.getTaxes() != null ? tx.getTaxes() : BigDecimal.ZERO;

        if (BUY_TYPES.contains(type)) {
            BigDecimal totalCost = tx.getGrossAmount().add(fees).add(taxes);
            BigDecimal unitCost = quantity.signum() > 0 ? totalCost.divide(quantity, MC) : BigDecimal.ZERO;
            state.lots.addLast(new Lot(quantity, unitCost));
            state.totalFees = state.totalFees.add(fees);
            state.totalTaxes = state.totalTaxes.add(taxes);
        } else if (SELL_TYPES.contains(type)) {
            BigDecimal remaining = quantity;
            BigDecimal costOfSold = BigDecimal.ZERO;
            while (remaining.signum() > 0) {
                Lot lot = state.lots.peekFirst();
                if (lot == null) {
                    throw new InsufficientUnitsException(
                            "Sale/redemption of " + quantity + " units on " + tx.getTransactionDate()
                                    + " exceeds available units held for this asset");
                }
                BigDecimal take = lot.quantity().min(remaining);
                costOfSold = costOfSold.add(take.multiply(lot.unitCost(), MC));
                BigDecimal lotRemainder = lot.quantity().subtract(take);
                state.lots.pollFirst();
                if (lotRemainder.signum() > 0) {
                    state.lots.addFirst(new Lot(lotRemainder, lot.unitCost()));
                }
                remaining = remaining.subtract(take);
            }
            BigDecimal proceeds = tx.getNetAmount();
            state.realizedGain = state.realizedGain.add(proceeds.subtract(costOfSold));
            state.totalFees = state.totalFees.add(fees);
            state.totalTaxes = state.totalTaxes.add(taxes);
        } else if (type == TransactionType.DIVIDEND) {
            state.dividendIncome = state.dividendIncome.add(tx.getNetAmount());
            state.realizedGain = state.realizedGain.add(tx.getNetAmount());
        } else if (type == TransactionType.FEE) {
            state.totalFees = state.totalFees.add(tx.getNetAmount());
            state.realizedGain = state.realizedGain.subtract(tx.getNetAmount());
        } else if (type == TransactionType.TAX) {
            state.totalTaxes = state.totalTaxes.add(tx.getNetAmount());
            state.realizedGain = state.realizedGain.subtract(tx.getNetAmount());
        } else if (type == TransactionType.ADJUSTMENT) {
            applyAdjustment(tx, state, quantity);
        }
    }

    private void applyAdjustment(PortfolioTransaction tx, AssetLedgerState state, BigDecimal quantity) {
        if (quantity.signum() >= 0) {
            // Upward correction (e.g. bonus units, opening-balance entry): added at the user-supplied cost,
            // or zero cost when none is supplied (Part 24 - never fabricate a historical price).
            BigDecimal unitCost = tx.getPricePerUnit() != null ? tx.getPricePerUnit()
                    : (quantity.signum() > 0 && tx.getGrossAmount() != null
                        ? tx.getGrossAmount().divide(quantity, MC) : BigDecimal.ZERO);
            if (quantity.signum() > 0) {
                state.lots.addLast(new Lot(quantity, unitCost));
            }
        } else {
            // Downward correction: remove units FIFO without recognizing gain/loss (non-cash, Part 24).
            BigDecimal remaining = quantity.negate();
            while (remaining.signum() > 0) {
                Lot lot = state.lots.peekFirst();
                if (lot == null) {
                    throw new InsufficientUnitsException(
                            "Adjustment removing " + remaining + " units on " + tx.getTransactionDate()
                                    + " exceeds available units held for this asset");
                }
                BigDecimal take = lot.quantity().min(remaining);
                BigDecimal lotRemainder = lot.quantity().subtract(take);
                state.lots.pollFirst();
                if (lotRemainder.signum() > 0) {
                    state.lots.addFirst(new Lot(lotRemainder, lot.unitCost()));
                }
                remaining = remaining.subtract(take);
            }
        }
    }

    private List<PortfolioTransaction> sorted(List<PortfolioTransaction> transactions) {
        return transactions.stream()
                .sorted(Comparator.comparing(PortfolioTransaction::getTransactionDate)
                        .thenComparing(tx -> tx.getCreatedAt() != null ? tx.getCreatedAt() : java.time.Instant.EPOCH))
                .toList();
    }

    private String assetKey(PortfolioTransaction tx) {
        return tx.getAssetType() + ":" + (tx.getAssetType() == AssetType.STOCK ? tx.getStockSymbol() : tx.getMutualFundSchemeCode());
    }
}
