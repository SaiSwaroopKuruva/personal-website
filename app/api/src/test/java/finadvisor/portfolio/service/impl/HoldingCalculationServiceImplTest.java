package finadvisor.portfolio.service.impl;

import finadvisor.portfolio.entity.AssetType;
import finadvisor.portfolio.entity.PortfolioTransaction;
import finadvisor.portfolio.entity.TransactionType;
import finadvisor.portfolio.exception.InsufficientUnitsException;
import finadvisor.portfolio.service.CashFlowEntry;
import finadvisor.portfolio.service.HoldingPosition;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HoldingCalculationServiceImplTest {

    private final HoldingCalculationServiceImpl service = new HoldingCalculationServiceImpl();

    private PortfolioTransaction tx(TransactionType type, LocalDate date, String qty, String price, String gross, String fees, String taxes) {
        return PortfolioTransaction.builder()
                .assetType(AssetType.STOCK)
                .stockSymbol("INFY")
                .stockExchange("NSE")
                .transactionType(type)
                .transactionDate(date)
                .quantity(new BigDecimal(qty))
                .pricePerUnit(price != null ? new BigDecimal(price) : null)
                .grossAmount(new BigDecimal(gross))
                .fees(new BigDecimal(fees))
                .taxes(new BigDecimal(taxes))
                .netAmount(computeNet(type, new BigDecimal(gross), new BigDecimal(fees), new BigDecimal(taxes)))
                .createdAt(date.atStartOfDay(java.time.ZoneOffset.UTC).toInstant())
                .build();
    }

    private BigDecimal computeNet(TransactionType type, BigDecimal gross, BigDecimal fees, BigDecimal taxes) {
        return switch (type) {
            case BUY, PURCHASE -> gross.add(fees).add(taxes);
            case SELL, REDEMPTION, DIVIDEND -> gross.subtract(fees).subtract(taxes);
            case ADJUSTMENT -> BigDecimal.ZERO;
            default -> gross;
        };
    }

    @Test
    void calculatePositions_singleBuyThenPartialSell_computesFifoRealizedGain() {
        List<PortfolioTransaction> ledger = List.of(
                tx(TransactionType.BUY, LocalDate.of(2024, 1, 1), "100", "100", "10000", "0", "0"),
                tx(TransactionType.SELL, LocalDate.of(2024, 6, 1), "40", "150", "6000", "0", "0"));

        List<HoldingPosition> positions = service.calculatePositions(ledger);

        assertThat(positions).hasSize(1);
        HoldingPosition position = positions.get(0);
        assertThat(position.quantity()).isEqualByComparingTo("60");
        assertThat(position.costBasis()).isEqualByComparingTo("6000"); // 60 units * 100 cost
        // realized gain = proceeds(6000) - cost of sold units(40*100=4000) = 2000
        assertThat(position.realizedGain()).isEqualByComparingTo("2000");
    }

    @Test
    void calculatePositions_fifoAcrossMultipleLots() {
        List<PortfolioTransaction> ledger = List.of(
                tx(TransactionType.BUY, LocalDate.of(2024, 1, 1), "50", "100", "5000", "0", "0"),
                tx(TransactionType.BUY, LocalDate.of(2024, 2, 1), "50", "120", "6000", "0", "0"),
                tx(TransactionType.SELL, LocalDate.of(2024, 3, 1), "70", "150", "10500", "0", "0"));

        HoldingPosition position = service.calculatePositions(ledger).get(0);

        // Sells 50 @100 + 20 @120 = 5000 + 2400 = 7400 cost; proceeds 10500 -> gain 3100
        assertThat(position.realizedGain()).isEqualByComparingTo("3100");
        assertThat(position.quantity()).isEqualByComparingTo("30");
        assertThat(position.costBasis()).isEqualByComparingTo("3600"); // 30 remaining @120
    }

    @Test
    void calculatePositions_oversell_throwsInsufficientUnits() {
        List<PortfolioTransaction> ledger = List.of(
                tx(TransactionType.BUY, LocalDate.of(2024, 1, 1), "10", "100", "1000", "0", "0"),
                tx(TransactionType.SELL, LocalDate.of(2024, 2, 1), "20", "100", "2000", "0", "0"));

        assertThatThrownBy(() -> service.calculatePositions(ledger)).isInstanceOf(InsufficientUnitsException.class);
    }

    @Test
    void calculatePositions_dividendAddsToRealizedGainWithoutAffectingQuantity() {
        List<PortfolioTransaction> ledger = List.of(
                tx(TransactionType.BUY, LocalDate.of(2024, 1, 1), "10", "100", "1000", "0", "0"),
                tx(TransactionType.DIVIDEND, LocalDate.of(2024, 6, 1), "0", null, "50", "0", "0"));

        HoldingPosition position = service.calculatePositions(ledger).get(0);

        assertThat(position.quantity()).isEqualByComparingTo("10");
        assertThat(position.dividendIncome()).isEqualByComparingTo("50");
        assertThat(position.realizedGain()).isEqualByComparingTo("50");
    }

    @Test
    void calculatePositions_feeAndTaxReduceRealizedGain() {
        List<PortfolioTransaction> ledger = List.of(
                tx(TransactionType.BUY, LocalDate.of(2024, 1, 1), "10", "100", "1000", "0", "0"),
                tx(TransactionType.FEE, LocalDate.of(2024, 2, 1), "0", null, "25", "0", "0"),
                tx(TransactionType.TAX, LocalDate.of(2024, 3, 1), "0", null, "15", "0", "0"));

        HoldingPosition position = service.calculatePositions(ledger).get(0);

        assertThat(position.realizedGain()).isEqualByComparingTo("-40");
    }

    @Test
    void calculatePositions_upwardAdjustmentAddsUnitsAtSuppliedCost() {
        List<PortfolioTransaction> ledger = List.of(
                tx(TransactionType.BUY, LocalDate.of(2024, 1, 1), "10", "100", "1000", "0", "0"),
                tx(TransactionType.ADJUSTMENT, LocalDate.of(2024, 2, 1), "5", "0", "0", "0", "0"));

        HoldingPosition position = service.calculatePositions(ledger).get(0);

        assertThat(position.quantity()).isEqualByComparingTo("15");
        // 10 units @100 + 5 units @0 (bonus) = cost basis unchanged at 1000
        assertThat(position.costBasis()).isEqualByComparingTo("1000");
    }

    @Test
    void calculatePositions_downwardAdjustmentRemovesUnitsWithoutGainImpact() {
        List<PortfolioTransaction> ledger = new java.util.ArrayList<>(List.of(
                tx(TransactionType.BUY, LocalDate.of(2024, 1, 1), "10", "100", "1000", "0", "0")));
        PortfolioTransaction negativeAdjustment = PortfolioTransaction.builder()
                .assetType(AssetType.STOCK).stockSymbol("INFY").stockExchange("NSE")
                .transactionType(TransactionType.ADJUSTMENT).transactionDate(LocalDate.of(2024, 2, 1))
                .quantity(new BigDecimal("-3")).grossAmount(BigDecimal.ZERO).fees(BigDecimal.ZERO).taxes(BigDecimal.ZERO)
                .netAmount(BigDecimal.ZERO).createdAt(Instant.now()).build();
        ledger.add(negativeAdjustment);

        HoldingPosition position = service.calculatePositions(ledger).get(0);

        assertThat(position.quantity()).isEqualByComparingTo("7");
        assertThat(position.realizedGain()).isEqualByComparingTo("0");
    }

    @Test
    void buildInvestorCashFlows_signConvention() {
        List<PortfolioTransaction> ledger = List.of(
                tx(TransactionType.BUY, LocalDate.of(2024, 1, 1), "10", "100", "1000", "0", "0"),
                tx(TransactionType.SELL, LocalDate.of(2024, 6, 1), "10", "120", "1200", "0", "0"),
                tx(TransactionType.DIVIDEND, LocalDate.of(2024, 3, 1), "0", null, "20", "0", "0"));

        List<CashFlowEntry> flows = service.buildInvestorCashFlows(ledger);

        assertThat(flows).hasSize(3);
        assertThat(flows.get(0).amount()).isEqualByComparingTo("-1000");
        assertThat(flows.stream().anyMatch(f -> f.amount().compareTo(new BigDecimal("1200")) == 0)).isTrue();
        assertThat(flows.stream().anyMatch(f -> f.amount().compareTo(new BigDecimal("20")) == 0)).isTrue();
    }

    @Test
    void validateNewTransaction_rejectsSaleExceedingHeldUnits() {
        PortfolioTransaction buy = tx(TransactionType.BUY, LocalDate.of(2024, 1, 1), "10", "100", "1000", "0", "0");
        PortfolioTransaction oversell = tx(TransactionType.SELL, LocalDate.of(2024, 2, 1), "15", "100", "1500", "0", "0");

        assertThatThrownBy(() -> service.validateNewTransaction(oversell, List.of(buy)))
                .isInstanceOf(InsufficientUnitsException.class);
    }
}
