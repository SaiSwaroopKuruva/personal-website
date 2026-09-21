package finadvisor.service.impl;

import finadvisor.config.CalculatorProperties;
import finadvisor.dto.calculator.SipCalculatorRequest;
import finadvisor.dto.calculator.SipCalculatorResponse;
import finadvisor.exception.InvalidCalculatorInputException;
import finadvisor.service.SipCalculatorService;
import finadvisor.util.FinancialDisclaimers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Standard SIP (annuity-due, monthly compounding) future value formula:
 * {@code FV = P * (((1+i)^n - 1) / i) * (1+i)}, where {@code i} is the monthly rate and {@code n} the
 * number of monthly installments. All arithmetic uses {@link BigDecimal} - never double/float - for
 * financial calculations.
 */
@Service
@RequiredArgsConstructor
public class SipCalculatorServiceImpl implements SipCalculatorService {

    private static final MathContext MC = new MathContext(20, RoundingMode.HALF_UP);
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal TWELVE = BigDecimal.valueOf(12);

    private final CalculatorProperties calculatorProperties;

    @Override
    public SipCalculatorResponse calculate(SipCalculatorRequest request) {
        validate(request);

        int months = request.durationYears() * 12;
        BigDecimal monthlyRate = request.expectedAnnualReturn().divide(TWELVE, MC).divide(HUNDRED, MC);
        BigDecimal totalInvestment = request.monthlyInvestment().multiply(BigDecimal.valueOf(months));

        BigDecimal futureValue;
        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            futureValue = totalInvestment;
        } else {
            BigDecimal onePlusRate = BigDecimal.ONE.add(monthlyRate);
            BigDecimal growthFactor = onePlusRate.pow(months, MC);
            futureValue = request.monthlyInvestment()
                    .multiply(growthFactor.subtract(BigDecimal.ONE).divide(monthlyRate, MC))
                    .multiply(onePlusRate, MC);
        }

        BigDecimal roundedFutureValue = futureValue.setScale(2, RoundingMode.HALF_UP);
        BigDecimal roundedInvestment = totalInvestment.setScale(2, RoundingMode.HALF_UP);
        BigDecimal estimatedReturns = roundedFutureValue.subtract(roundedInvestment);

        return new SipCalculatorResponse(request.monthlyInvestment(), request.expectedAnnualReturn(),
                request.durationYears(), roundedInvestment, estimatedReturns, roundedFutureValue, true,
                FinancialDisclaimers.CALCULATOR_ESTIMATE);
    }

    private void validate(SipCalculatorRequest request) {
        if (request.monthlyInvestment().compareTo(calculatorProperties.getMaxAmount()) > 0) {
            throw new InvalidCalculatorInputException("Monthly investment exceeds the maximum supported amount");
        }
        if (request.durationYears() > calculatorProperties.getMaxDurationYears()) {
            throw new InvalidCalculatorInputException("Duration exceeds the maximum supported years");
        }
        if (request.expectedAnnualReturn().compareTo(calculatorProperties.getMaxAnnualReturnPercentage()) > 0) {
            throw new InvalidCalculatorInputException("Expected annual return exceeds the maximum supported percentage");
        }
    }
}
