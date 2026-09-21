package finadvisor.service.impl;

import finadvisor.config.CalculatorProperties;
import finadvisor.dto.calculator.SipCalculatorRequest;
import finadvisor.exception.InvalidCalculatorInputException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SipCalculatorServiceImplTest {

    private SipCalculatorServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SipCalculatorServiceImpl(new CalculatorProperties());
    }

    @Test
    void calculate_shouldComputeFutureValueUsingAnnuityDueFormula() {
        var request = new SipCalculatorRequest(new BigDecimal("10000"), new BigDecimal("12"), 10);

        var response = service.calculate(request);

        assertThat(response.totalInvestment()).isEqualByComparingTo("1200000");
        // Standard SIP annuity-due formula: FV = P * (((1+i)^n - 1)/i) * (1+i), i = 1%/month, n = 120
        assertThat(response.futureValue()).isGreaterThan(response.totalInvestment());
        assertThat(response.estimatedReturns()).isEqualByComparingTo(response.futureValue().subtract(response.totalInvestment()));
        assertThat(response.estimate()).isTrue();
        assertThat(response.disclaimer()).isNotBlank();
    }

    @Test
    void calculate_withZeroReturn_shouldEqualTotalInvestment() {
        var request = new SipCalculatorRequest(new BigDecimal("5000"), BigDecimal.ZERO, 5);

        var response = service.calculate(request);

        assertThat(response.futureValue()).isEqualByComparingTo("300000");
        assertThat(response.estimatedReturns()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void calculate_shouldRejectAmountAboveConfiguredMax() {
        CalculatorProperties properties = new CalculatorProperties();
        properties.setMaxAmount(new BigDecimal("100000"));
        service = new SipCalculatorServiceImpl(properties);

        var request = new SipCalculatorRequest(new BigDecimal("200000"), new BigDecimal("10"), 5);

        assertThatThrownBy(() -> service.calculate(request)).isInstanceOf(InvalidCalculatorInputException.class);
    }

    @Test
    void calculate_shouldRejectDurationAboveConfiguredMax() {
        var request = new SipCalculatorRequest(new BigDecimal("1000"), new BigDecimal("10"), 100);

        assertThatThrownBy(() -> service.calculate(request)).isInstanceOf(InvalidCalculatorInputException.class);
    }
}
