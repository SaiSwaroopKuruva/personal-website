package finadvisor.service.impl;

import finadvisor.config.CalculatorProperties;
import finadvisor.dto.calculator.LumpsumCalculatorRequest;
import finadvisor.dto.calculator.LumpsumCalculatorResponse;
import finadvisor.exception.InvalidCalculatorInputException;
import finadvisor.service.LumpsumCalculatorService;
import finadvisor.util.FinancialDisclaimers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/** Standard lumpsum future value formula with annual compounding: {@code FV = P * (1 + r)^t}. */
@Service
@RequiredArgsConstructor
public class LumpsumCalculatorServiceImpl implements LumpsumCalculatorService {

    private static final MathContext MC = new MathContext(20, RoundingMode.HALF_UP);
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final CalculatorProperties calculatorProperties;

    @Override
    public LumpsumCalculatorResponse calculate(LumpsumCalculatorRequest request) {
        validate(request);

        BigDecimal annualRate = request.expectedAnnualReturn().divide(HUNDRED, MC);
        BigDecimal growthFactor = BigDecimal.ONE.add(annualRate).pow(request.durationYears(), MC);
        BigDecimal futureValue = request.principal().multiply(growthFactor, MC).setScale(2, RoundingMode.HALF_UP);
        BigDecimal invested = request.principal().setScale(2, RoundingMode.HALF_UP);
        BigDecimal estimatedReturns = futureValue.subtract(invested);

        return new LumpsumCalculatorResponse(request.principal(), request.expectedAnnualReturn(),
                request.durationYears(), invested, estimatedReturns, futureValue, true,
                FinancialDisclaimers.CALCULATOR_ESTIMATE);
    }

    private void validate(LumpsumCalculatorRequest request) {
        if (request.principal().compareTo(calculatorProperties.getMaxAmount()) > 0) {
            throw new InvalidCalculatorInputException("Principal exceeds the maximum supported amount");
        }
        if (request.durationYears() > calculatorProperties.getMaxDurationYears()) {
            throw new InvalidCalculatorInputException("Duration exceeds the maximum supported years");
        }
        if (request.expectedAnnualReturn().compareTo(calculatorProperties.getMaxAnnualReturnPercentage()) > 0) {
            throw new InvalidCalculatorInputException("Expected annual return exceeds the maximum supported percentage");
        }
    }
}
