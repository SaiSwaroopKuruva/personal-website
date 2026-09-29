package finadvisor.portfolio.service.impl;

import finadvisor.portfolio.dto.XirrResponse;
import finadvisor.portfolio.dto.XirrStatus;
import finadvisor.portfolio.service.CashFlowEntry;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class XirrServiceImplTest {

    private final XirrServiceImpl service = new XirrServiceImpl();

    @Test
    void calculate_oneInitialInvestment_shouldMatchSimpleAnnualizedReturn() {
        // Invest 100000, worth 121000 exactly one year later -> XIRR should be ~21%.
        List<CashFlowEntry> flows = List.of(
                new CashFlowEntry(LocalDate.of(2024, 1, 1), new BigDecimal("-100000")),
                new CashFlowEntry(LocalDate.of(2025, 1, 1), new BigDecimal("121000")));

        XirrResponse result = service.calculate(flows);

        assertThat(result.status()).isEqualTo(XirrStatus.CALCULATED);
        assertThat(result.xirrPercent()).isCloseTo(new BigDecimal("21"), org.assertj.core.data.Offset.offset(new BigDecimal("0.5")));
    }

    @Test
    void calculate_multipleInvestmentsOnDifferentDates_shouldConverge() {
        List<CashFlowEntry> flows = List.of(
                new CashFlowEntry(LocalDate.of(2023, 1, 1), new BigDecimal("-50000")),
                new CashFlowEntry(LocalDate.of(2023, 7, 1), new BigDecimal("-30000")),
                new CashFlowEntry(LocalDate.of(2024, 1, 1), new BigDecimal("-20000")),
                new CashFlowEntry(LocalDate.of(2025, 1, 1), new BigDecimal("115000")));

        XirrResponse result = service.calculate(flows);

        assertThat(result.status()).isEqualTo(XirrStatus.CALCULATED);
        assertThat(result.xirrPercent()).isNotNull();
    }

    @Test
    void calculate_partialWithdrawalThenTerminalValue_shouldConverge() {
        List<CashFlowEntry> flows = List.of(
                new CashFlowEntry(LocalDate.of(2022, 1, 1), new BigDecimal("-100000")),
                new CashFlowEntry(LocalDate.of(2023, 1, 1), new BigDecimal("20000")),
                new CashFlowEntry(LocalDate.of(2024, 1, 1), new BigDecimal("100000")));

        XirrResponse result = service.calculate(flows);

        assertThat(result.status()).isEqualTo(XirrStatus.CALCULATED);
    }

    @Test
    void calculate_withDividends_shouldConverge() {
        List<CashFlowEntry> flows = List.of(
                new CashFlowEntry(LocalDate.of(2023, 1, 1), new BigDecimal("-100000")),
                new CashFlowEntry(LocalDate.of(2023, 6, 1), new BigDecimal("2000")),
                new CashFlowEntry(LocalDate.of(2023, 12, 1), new BigDecimal("2000")),
                new CashFlowEntry(LocalDate.of(2024, 1, 1), new BigDecimal("105000")));

        XirrResponse result = service.calculate(flows);

        assertThat(result.status()).isEqualTo(XirrStatus.CALCULATED);
        assertThat(result.xirrPercent()).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    void calculate_zeroGain_shouldBeApproximatelyZeroPercent() {
        List<CashFlowEntry> flows = List.of(
                new CashFlowEntry(LocalDate.of(2024, 1, 1), new BigDecimal("-100000")),
                new CashFlowEntry(LocalDate.of(2025, 1, 1), new BigDecimal("100000")));

        XirrResponse result = service.calculate(flows);

        assertThat(result.status()).isEqualTo(XirrStatus.CALCULATED);
        assertThat(result.xirrPercent()).isCloseTo(BigDecimal.ZERO, org.assertj.core.data.Offset.offset(new BigDecimal("0.1")));
    }

    @Test
    void calculate_loss_shouldBeNegativePercent() {
        List<CashFlowEntry> flows = List.of(
                new CashFlowEntry(LocalDate.of(2024, 1, 1), new BigDecimal("-100000")),
                new CashFlowEntry(LocalDate.of(2025, 1, 1), new BigDecimal("80000")));

        XirrResponse result = service.calculate(flows);

        assertThat(result.status()).isEqualTo(XirrStatus.CALCULATED);
        assertThat(result.xirrPercent()).isLessThan(BigDecimal.ZERO);
    }

    @Test
    void calculate_missingCashFlows_shouldReturnInsufficientData() {
        XirrResponse result = service.calculate(List.of());
        assertThat(result.status()).isEqualTo(XirrStatus.INSUFFICIENT_DATA);
        assertThat(result.xirrPercent()).isNull();
    }

    @Test
    void calculate_singleCashFlow_shouldReturnInsufficientData() {
        XirrResponse result = service.calculate(List.of(new CashFlowEntry(LocalDate.now(), new BigDecimal("-1000"))));
        assertThat(result.status()).isEqualTo(XirrStatus.INSUFFICIENT_DATA);
    }

    @Test
    void calculate_allSameSign_shouldReturnNoValidSolution() {
        List<CashFlowEntry> flows = List.of(
                new CashFlowEntry(LocalDate.of(2024, 1, 1), new BigDecimal("-1000")),
                new CashFlowEntry(LocalDate.of(2024, 6, 1), new BigDecimal("-500")));

        XirrResponse result = service.calculate(flows);

        assertThat(result.status()).isEqualTo(XirrStatus.NO_VALID_SOLUTION);
        assertThat(result.xirrPercent()).isNull();
    }

    @Test
    void calculate_nullDate_shouldReturnInsufficientData() {
        List<CashFlowEntry> flows = new java.util.ArrayList<>();
        flows.add(new CashFlowEntry(null, new BigDecimal("-1000")));
        flows.add(new CashFlowEntry(LocalDate.now(), new BigDecimal("1000")));

        XirrResponse result = service.calculate(flows);

        assertThat(result.status()).isEqualTo(XirrStatus.INSUFFICIENT_DATA);
    }

    @Test
    void calculate_neverReturnsNaNOrInfinite() {
        List<CashFlowEntry> flows = List.of(
                new CashFlowEntry(LocalDate.of(2024, 1, 1), new BigDecimal("-1")),
                new CashFlowEntry(LocalDate.of(2024, 1, 2), new BigDecimal("100000000")));

        XirrResponse result = service.calculate(flows);

        if (result.xirrPercent() != null) {
            assertThat(result.xirrPercent().doubleValue()).isFinite();
        }
    }
}
