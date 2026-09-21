package finadvisor.mutualfund.service.impl;

import finadvisor.mutualfund.config.CalculatorProperties;
import finadvisor.mutualfund.dto.calculator.LumpsumCalculatorRequest;
import finadvisor.mutualfund.exception.InvalidCalculatorInputException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LumpsumCalculatorServiceImplTest {

    private LumpsumCalculatorServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new LumpsumCalculatorServiceImpl(new CalculatorProperties());
    }

    @Test
    void calculate_shouldCompoundAnnually() {
        var request = new LumpsumCalculatorRequest(new BigDecimal("100000"), new BigDecimal("10"), 3);

        var response = service.calculate(request);

        // 100000 * 1.1^3 = 133100.00
        assertThat(response.futureValue()).isEqualByComparingTo("133100.00");
        assertThat(response.investedAmount()).isEqualByComparingTo("100000.00");
        assertThat(response.estimatedReturns()).isEqualByComparingTo("33100.00");
    }

    @Test
    void calculate_shouldRejectReturnAboveConfiguredMax() {
        CalculatorProperties properties = new CalculatorProperties();
        properties.setMaxAnnualReturnPercentage(new BigDecimal("20"));
        service = new LumpsumCalculatorServiceImpl(properties);

        var request = new LumpsumCalculatorRequest(new BigDecimal("10000"), new BigDecimal("25"), 5);

        assertThatThrownBy(() -> service.calculate(request)).isInstanceOf(InvalidCalculatorInputException.class);
    }
}
