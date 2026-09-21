package finadvisor.service.impl;

import finadvisor.config.CalculatorProperties;
import finadvisor.dto.calculator.SwpCalculatorRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class SwpCalculatorServiceImplTest {

    private SwpCalculatorServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SwpCalculatorServiceImpl(new CalculatorProperties());
    }

    @Test
    void calculate_withSustainableWithdrawal_shouldNotExhaustCorpus() {
        var request = new SwpCalculatorRequest(new BigDecimal("1000000"), new BigDecimal("2000"), new BigDecimal("8"), 5);

        var response = service.calculate(request);

        assertThat(response.exhausted()).isFalse();
        assertThat(response.exhaustedAfterMonths()).isNull();
        assertThat(response.remainingValue()).isGreaterThan(BigDecimal.ZERO);
        assertThat(response.totalWithdrawn()).isEqualByComparingTo(new BigDecimal("2000").multiply(BigDecimal.valueOf(60)));
    }

    @Test
    void calculate_withAggressiveWithdrawal_shouldDetectExhaustion() {
        var request = new SwpCalculatorRequest(new BigDecimal("100000"), new BigDecimal("50000"), new BigDecimal("6"), 5);

        var response = service.calculate(request);

        assertThat(response.exhausted()).isTrue();
        assertThat(response.exhaustedAfterMonths()).isNotNull().isLessThanOrEqualTo(60);
        assertThat(response.remainingValue()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void calculate_withZeroWithdrawal_shouldOnlyGrowCorpus() {
        var request = new SwpCalculatorRequest(new BigDecimal("100000"), BigDecimal.ZERO, new BigDecimal("10"), 1);

        var response = service.calculate(request);

        assertThat(response.totalWithdrawn()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.remainingValue()).isGreaterThan(request.initialInvestment());
    }
}
