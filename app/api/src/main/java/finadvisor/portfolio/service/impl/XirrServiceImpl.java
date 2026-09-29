package finadvisor.portfolio.service.impl;

import finadvisor.portfolio.dto.XirrResponse;
import finadvisor.portfolio.dto.XirrStatus;
import finadvisor.portfolio.service.CashFlowEntry;
import finadvisor.portfolio.service.XirrService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

/**
 * XIRR via Newton-Raphson with a bisection fallback (Part 12). Cash-flow sign convention (documented here
 * and mirrored in {@link finadvisor.portfolio.service.impl.HoldingCalculationServiceImpl}):
 * contributions/fees/taxes are negative, withdrawals/dividends/the terminal portfolio value are positive.
 * Root-finding is done in {@code double} - this is a numerical algorithm over a rate, not a stored money
 * amount, so BigDecimal is not required here; only the final percentage is rounded into a BigDecimal.
 */
@Service
public class XirrServiceImpl implements XirrService {

    private static final int MAX_NEWTON_ITERATIONS = 100;
    private static final int MAX_BISECTION_ITERATIONS = 200;
    private static final double TOLERANCE = 1e-7;
    private static final double DAYS_PER_YEAR = 365.0;
    private static final double MIN_RATE = -0.999999;
    private static final double MAX_RATE = 100.0;

    @Override
    public XirrResponse calculate(List<CashFlowEntry> cashFlows) {
        if (cashFlows == null || cashFlows.size() < 2) {
            return result(null, XirrStatus.INSUFFICIENT_DATA,
                    "At least two dated cash flows (an investment and a valuation) are required to calculate XIRR.");
        }

        for (CashFlowEntry entry : cashFlows) {
            if (entry.date() == null || entry.amount() == null) {
                return result(null, XirrStatus.INSUFFICIENT_DATA, "Cash flows must have a valid date and amount.");
            }
        }

        List<CashFlowEntry> sorted = cashFlows.stream()
                .sorted(Comparator.comparing(CashFlowEntry::date))
                .toList();

        boolean hasNegative = sorted.stream().anyMatch(c -> c.amount().signum() < 0);
        boolean hasPositive = sorted.stream().anyMatch(c -> c.amount().signum() > 0);
        if (!hasNegative || !hasPositive) {
            return result(null, XirrStatus.NO_VALID_SOLUTION,
                    "XIRR requires at least one contribution (negative) and one inflow/valuation (positive) cash flow.");
        }

        LocalDate epoch = sorted.get(0).date();
        double[] amounts = new double[sorted.size()];
        double[] years = new double[sorted.size()];
        for (int i = 0; i < sorted.size(); i++) {
            amounts[i] = sorted.get(i).amount().doubleValue();
            years[i] = ChronoUnit.DAYS.between(epoch, sorted.get(i).date()) / DAYS_PER_YEAR;
        }

        Double rate = solveNewton(amounts, years);
        if (rate == null) {
            rate = solveBisection(amounts, years);
        }

        if (rate == null || !Double.isFinite(rate)) {
            return result(null, XirrStatus.NON_CONVERGENT,
                    "XIRR could not converge on a solution for the given cash flows.");
        }

        BigDecimal percent = BigDecimal.valueOf(rate * 100).setScale(4, RoundingMode.HALF_UP);
        return result(percent, XirrStatus.CALCULATED, null);
    }

    private Double solveNewton(double[] amounts, double[] years) {
        double rate = 0.1;
        for (int iter = 0; iter < MAX_NEWTON_ITERATIONS; iter++) {
            double f = npv(amounts, years, rate);
            double df = npvDerivative(amounts, years, rate);
            if (!Double.isFinite(f) || !Double.isFinite(df) || Math.abs(df) < 1e-12) {
                return null;
            }
            double nextRate = rate - f / df;
            if (!Double.isFinite(nextRate) || nextRate <= MIN_RATE) {
                return null;
            }
            if (Math.abs(nextRate - rate) < TOLERANCE) {
                return nextRate;
            }
            rate = nextRate;
        }
        return null;
    }

    private Double solveBisection(double[] amounts, double[] years) {
        double low = MIN_RATE;
        double high = MAX_RATE;
        double fLow = npv(amounts, years, low);
        double fHigh = npv(amounts, years, high);
        if (!Double.isFinite(fLow) || !Double.isFinite(fHigh) || Math.signum(fLow) == Math.signum(fHigh)) {
            return null;
        }
        double mid = 0;
        for (int iter = 0; iter < MAX_BISECTION_ITERATIONS; iter++) {
            mid = (low + high) / 2;
            double fMid = npv(amounts, years, mid);
            if (!Double.isFinite(fMid)) {
                return null;
            }
            if (Math.abs(fMid) < TOLERANCE || (high - low) / 2 < TOLERANCE) {
                return mid;
            }
            if (Math.signum(fMid) == Math.signum(fLow)) {
                low = mid;
                fLow = fMid;
            } else {
                high = mid;
            }
        }
        return mid;
    }

    private double npv(double[] amounts, double[] years, double rate) {
        double total = 0;
        for (int i = 0; i < amounts.length; i++) {
            total += amounts[i] / Math.pow(1 + rate, years[i]);
        }
        return total;
    }

    private double npvDerivative(double[] amounts, double[] years, double rate) {
        double total = 0;
        for (int i = 0; i < amounts.length; i++) {
            if (years[i] == 0) {
                continue;
            }
            total += -years[i] * amounts[i] / Math.pow(1 + rate, years[i] + 1);
        }
        return total;
    }

    private XirrResponse result(BigDecimal percent, XirrStatus status, String message) {
        return new XirrResponse(percent, status, message, Instant.now());
    }
}
