package finadvisor.service.impl;

import finadvisor.config.CalculatorProperties;
import finadvisor.dto.calculator.SwpCalculatorRequest;
import finadvisor.dto.calculator.SwpCalculatorResponse;
import finadvisor.exception.InvalidCalculatorInputException;
import finadvisor.service.SwpCalculatorService;
import finadvisor.util.FinancialDisclaimers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Simulates a Systematic Withdrawal Plan month-by-month (growth applied, then withdrawal capped at the
 * remaining balance) so early exhaustion of the corpus is detected rather than assumed away by a closed
 * form. All arithmetic uses {@link BigDecimal}.
 */
@Service
@RequiredArgsConstructor
public class SwpCalculatorServiceImpl implements SwpCalculatorService {

    private static final MathContext MC = new MathContext(20, RoundingMode.HALF_UP);
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal TWELVE = BigDecimal.valueOf(12);

    private final CalculatorProperties calculatorProperties;

    @Override
    public SwpCalculatorResponse calculate(SwpCalculatorRequest request) {
        validate(request);

        BigDecimal monthlyRate = request.expectedAnnualReturn().divide(TWELVE, MC).divide(HUNDRED, MC);
        int months = request.durationYears() * 12;

        BigDecimal balance = request.initialInvestment();
        BigDecimal totalWithdrawn = BigDecimal.ZERO;
        boolean exhausted = false;
        Integer exhaustedAfterMonths = null;

        for (int month = 1; month <= months; month++) {
            balance = balance.multiply(BigDecimal.ONE.add(monthlyRate), MC);
            if (balance.compareTo(BigDecimal.ZERO) <= 0) {
                balance = BigDecimal.ZERO;
                exhausted = true;
                exhaustedAfterMonths = month;
                break;
            }
            BigDecimal withdrawal = request.withdrawalPerMonth().min(balance);
            balance = balance.subtract(withdrawal);
            totalWithdrawn = totalWithdrawn.add(withdrawal);
            if (balance.compareTo(BigDecimal.ZERO) <= 0) {
                balance = BigDecimal.ZERO;
                exhausted = true;
                exhaustedAfterMonths = month;
                break;
            }
        }

        BigDecimal remainingValue = balance.setScale(2, RoundingMode.HALF_UP);
        BigDecimal roundedWithdrawn = totalWithdrawn.setScale(2, RoundingMode.HALF_UP);
        BigDecimal estimatedGrowth = remainingValue.add(roundedWithdrawn).subtract(request.initialInvestment());

        return new SwpCalculatorResponse(request.initialInvestment(), request.withdrawalPerMonth(),
                request.expectedAnnualReturn(), request.durationYears(), roundedWithdrawn, remainingValue,
                estimatedGrowth, exhausted, exhaustedAfterMonths, true, FinancialDisclaimers.CALCULATOR_ESTIMATE);
    }

    private void validate(SwpCalculatorRequest request) {
        if (request.initialInvestment().compareTo(calculatorProperties.getMaxAmount()) > 0) {
            throw new InvalidCalculatorInputException("Initial investment exceeds the maximum supported amount");
        }
        if (request.withdrawalPerMonth().compareTo(calculatorProperties.getMaxAmount()) > 0) {
            throw new InvalidCalculatorInputException("Monthly withdrawal exceeds the maximum supported amount");
        }
        if (request.durationYears() > calculatorProperties.getMaxDurationYears()) {
            throw new InvalidCalculatorInputException("Duration exceeds the maximum supported years");
        }
        if (request.expectedAnnualReturn().compareTo(calculatorProperties.getMaxAnnualReturnPercentage()) > 0) {
            throw new InvalidCalculatorInputException("Expected annual return exceeds the maximum supported percentage");
        }
    }
}
