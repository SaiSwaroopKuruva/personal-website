package finadvisor.mutualfund.provider.demo;

import finadvisor.mutualfund.provider.MutualFundDataProvider;
import finadvisor.mutualfund.provider.ProviderFundData;
import finadvisor.mutualfund.provider.ProviderHoldingData;
import finadvisor.mutualfund.provider.ProviderManagerData;
import finadvisor.mutualfund.provider.ProviderNavPoint;
import finadvisor.mutualfund.provider.ProviderResponse;
import finadvisor.mutualfund.provider.ProviderReturnData;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * DEVELOPMENT/DEMO ONLY provider. Simulates an external mutual-fund market-data feed for a fixed catalog
 * of clearly fictional schemes so the sync architecture (see {@code MutualFundDataSyncService}) can be
 * exercised end-to-end without a real vendor contract or API key. NAV values are generated deterministically
 * from the scheme code and the current date (a drift + sine-wave formula), never from real market feeds.
 * Replace with a real {@link MutualFundDataProvider} implementation (configured via
 * {@code mutualfund.provider.name}) before using this platform with real users/data.
 */
@Component
@ConditionalOnProperty(prefix = "mutualfund.provider", name = "name", havingValue = "demo", matchIfMissing = true)
public class DemoMutualFundDataProvider implements MutualFundDataProvider {

    private static final LocalDate REFERENCE_INCEPTION = LocalDate.now().minusYears(3);

    private record FundParams(String schemeCode, String isin, String amcCode, String amcName, String schemeName,
                               String shortName, String category, String subCategory, String assetClass,
                               String riskLevel, String benchmark, BigDecimal expenseRatio, String exitLoad,
                               BigDecimal minimumLumpsum, BigDecimal minimumSip, BigDecimal baseAum,
                               BigDecimal anchorNav, double annualDrift, double amplitude, int waveDays,
                               String fundManager) {
    }

    private static final List<FundParams> CATALOG = List.of(
            new FundParams("DEMO001", "INE000A01011", "NILGIRI", "Nilgiri Mutual Fund", "Nilgiri Bluechip Equity Fund",
                    "Nilgiri Bluechip", "Equity", "Large Cap", "EQUITY", "VERY_HIGH", "Nifty 100 TRI",
                    new BigDecimal("1.05"), "1% if redeemed within 365 days", new BigDecimal("5000"), new BigDecimal("500"),
                    new BigDecimal("12450.75"), new BigDecimal("14.20"), 0.13, 0.020, 47, "Ananya Rao"),
            new FundParams("DEMO002", "INE000A01029", "NILGIRI", "Nilgiri Mutual Fund", "Nilgiri Flexi Cap Fund",
                    "Nilgiri Flexi Cap", "Equity", "Flexi Cap", "EQUITY", "VERY_HIGH", "Nifty 500 TRI",
                    new BigDecimal("0.85"), "1% if redeemed within 365 days", new BigDecimal("5000"), new BigDecimal("500"),
                    new BigDecimal("9820.40"), new BigDecimal("14.80"), 0.14, 0.022, 53, "Karthik Subramaniam"),
            new FundParams("DEMO003", "INE000A01037", "SUNDARA", "Sundara Asset Management", "Sundara Midcap Growth Fund",
                    "Sundara Midcap", "Equity", "Mid Cap", "EQUITY", "VERY_HIGH", "Nifty Midcap 150 TRI",
                    new BigDecimal("1.15"), "1% if redeemed within 365 days", new BigDecimal("5000"), new BigDecimal("500"),
                    new BigDecimal("6210.60"), new BigDecimal("15.70"), 0.16, 0.030, 41, "Priya Deshmukh"),
            new FundParams("DEMO004", "INE000A01045", "SUNDARA", "Sundara Asset Management", "Sundara Smallcap Opportunities Fund",
                    "Sundara Smallcap", "Equity", "Small Cap", "EQUITY", "VERY_HIGH", "Nifty Smallcap 250 TRI",
                    new BigDecimal("1.35"), "1% if redeemed within 365 days", new BigDecimal("5000"), new BigDecimal("500"),
                    new BigDecimal("3125.90"), new BigDecimal("16.40"), 0.18, 0.040, 37, "Rohan Mehta"),
            new FundParams("DEMO005", "INE000A01052", "VAIDIK", "Vaidik Capital AMC", "Vaidik Tax Saver Fund",
                    "Vaidik Tax Saver", "Equity", "ELSS", "EQUITY", "VERY_HIGH", "Nifty 50 TRI",
                    new BigDecimal("0.95"), "Nil (lock-in of 3 years)", new BigDecimal("500"), new BigDecimal("500"),
                    new BigDecimal("4520.30"), new BigDecimal("14.40"), 0.13, 0.021, 45, "Meera Iyer"),
            new FundParams("DEMO006", "INE000A01060", "VAIDIK", "Vaidik Capital AMC", "Vaidik Liquid Fund",
                    "Vaidik Liquid", "Debt", "Liquid", "DEBT", "LOW", "CRISIL Liquid Fund Index",
                    new BigDecimal("0.20"), "Nil", new BigDecimal("5000"), new BigDecimal("1000"),
                    new BigDecimal("15840.00"), new BigDecimal("12.10"), 0.065, 0.002, 29, "Arjun Nair"),
            new FundParams("DEMO007", "INE000A01078", "TRIVENI", "Triveni Investment Managers", "Triveni Short Duration Debt Fund",
                    "Triveni Short Duration", "Debt", "Short Duration", "DEBT", "MODERATE", "CRISIL Short Duration Debt Index",
                    new BigDecimal("0.45"), "0.25% if redeemed within 30 days", new BigDecimal("5000"), new BigDecimal("1000"),
                    new BigDecimal("4380.20"), new BigDecimal("12.40"), 0.075, 0.004, 31, "Divya Krishnan"),
            new FundParams("DEMO008", "INE000A01086", "TRIVENI", "Triveni Investment Managers", "Triveni Balanced Advantage Fund",
                    "Triveni BAF", "Hybrid", "Balanced Advantage", "HYBRID", "MODERATELY_HIGH", "NIFTY 50 Hybrid Composite Debt 50:50 Index",
                    new BigDecimal("1.10"), "1% if redeemed within 365 days", new BigDecimal("5000"), new BigDecimal("500"),
                    new BigDecimal("7650.50"), new BigDecimal("13.30"), 0.10, 0.012, 39, "Vikram Chandran"),
            new FundParams("DEMO009", "INE000A01094", "KAVERI", "Kaveri Fund House", "Kaveri Aggressive Hybrid Fund",
                    "Kaveri Aggressive Hybrid", "Hybrid", "Aggressive Hybrid", "HYBRID", "HIGH", "CRISIL Hybrid 35+65 Aggressive Index",
                    new BigDecimal("1.20"), "1% if redeemed within 365 days", new BigDecimal("5000"), new BigDecimal("500"),
                    new BigDecimal("5230.80"), new BigDecimal("14.00"), 0.12, 0.018, 43, "Sneha Bhatt"),
            new FundParams("DEMO010", "INE000A01102", "KAVERI", "Kaveri Fund House", "Kaveri Nifty 50 Index Fund",
                    "Kaveri Nifty Index", "Equity", "Index Funds", "EQUITY", "VERY_HIGH", "Nifty 50 TRI",
                    new BigDecimal("0.20"), "Nil", new BigDecimal("5000"), new BigDecimal("500"),
                    new BigDecimal("2980.10"), new BigDecimal("14.20"), 0.125, 0.019, 49, "Aditya Menon"));

    @Override
    public String getProviderName() {
        return "demo-seed";
    }

    @Override
    public ProviderResponse<List<ProviderFundData>> searchFunds(String query) {
        String needle = query == null ? "" : query.trim().toLowerCase();
        List<ProviderFundData> matches = CATALOG.stream()
                .filter(f -> needle.isEmpty()
                        || f.schemeName().toLowerCase().contains(needle)
                        || f.category().toLowerCase().contains(needle))
                .map(f -> toFundData(f, LocalDate.now()))
                .toList();
        return ProviderResponse.ok(matches, getProviderName());
    }

    @Override
    public ProviderResponse<ProviderFundData> getFundDetails(String schemeCode) {
        return findParams(schemeCode)
                .map(f -> ProviderResponse.ok(toFundData(f, LocalDate.now()), getProviderName()))
                .orElseGet(() -> ProviderResponse.failure("Unknown scheme code: " + schemeCode, getProviderName()));
    }

    @Override
    public ProviderResponse<ProviderNavPoint> getCurrentNav(String schemeCode) {
        return findParams(schemeCode)
                .map(f -> ProviderResponse.ok(new ProviderNavPoint(schemeCode, navOn(f, LocalDate.now()), LocalDate.now()), getProviderName()))
                .orElseGet(() -> ProviderResponse.failure("Unknown scheme code: " + schemeCode, getProviderName()));
    }

    @Override
    public ProviderResponse<List<ProviderNavPoint>> getHistoricalNav(String schemeCode, LocalDate from, LocalDate to) {
        Optional<FundParams> params = findParams(schemeCode);
        if (params.isEmpty()) {
            return ProviderResponse.failure("Unknown scheme code: " + schemeCode, getProviderName());
        }
        List<ProviderNavPoint> points = new ArrayList<>();
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            if (date.getDayOfWeek().getValue() < 6) {
                points.add(new ProviderNavPoint(schemeCode, navOn(params.get(), date), date));
            }
        }
        return ProviderResponse.ok(points, getProviderName());
    }

    @Override
    public ProviderResponse<List<ProviderHoldingData>> getHoldings(String schemeCode) {
        if (findParams(schemeCode).isEmpty()) {
            return ProviderResponse.failure("Unknown scheme code: " + schemeCode, getProviderName());
        }
        return ProviderResponse.ok(DemoReferenceData.holdingsFor(schemeCode), getProviderName());
    }

    @Override
    public ProviderResponse<List<ProviderManagerData>> getFundManagers(String schemeCode) {
        return findParams(schemeCode)
                .map(f -> ProviderResponse.ok(
                        List.of(new ProviderManagerData(f.fundManager(), "Fund Manager", 10, REFERENCE_INCEPTION,
                                "Fictional demo fund manager profile used for development and testing only.", true)),
                        getProviderName()))
                .orElseGet(() -> ProviderResponse.failure("Unknown scheme code: " + schemeCode, getProviderName()));
    }

    @Override
    public ProviderResponse<List<ProviderReturnData>> getFundReturns(String schemeCode) {
        Optional<FundParams> params = findParams(schemeCode);
        if (params.isEmpty()) {
            return ProviderResponse.failure("Unknown scheme code: " + schemeCode, getProviderName());
        }
        FundParams f = params.get();
        LocalDate today = LocalDate.now();
        Map<String, Integer> periodDays = Map.of("1D", 1, "1W", 7, "1M", 30, "3M", 91, "6M", 182, "1Y", 365, "3Y", 1095);
        List<ProviderReturnData> returns = new ArrayList<>();
        BigDecimal endNav = navOn(f, today);
        periodDays.forEach((period, days) -> {
            LocalDate startDate = today.minusDays(days);
            if (startDate.isBefore(REFERENCE_INCEPTION)) {
                return;
            }
            BigDecimal startNav = navOn(f, startDate);
            boolean annualized = days >= 365;
            BigDecimal pct = annualized
                    ? annualizedReturn(startNav, endNav, days)
                    : endNav.divide(startNav, 8, RoundingMode.HALF_UP).subtract(BigDecimal.ONE)
                            .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
            returns.add(new ProviderReturnData(period, pct, annualized, today));
        });
        long sinceInceptionDays = ChronoUnit.DAYS.between(REFERENCE_INCEPTION, today);
        boolean sinceInceptionAnnualized = sinceInceptionDays >= 365;
        BigDecimal sinceInceptionPct = sinceInceptionAnnualized
                ? annualizedReturn(BigDecimal.TEN, endNav, sinceInceptionDays)
                : endNav.divide(BigDecimal.TEN, 8, RoundingMode.HALF_UP).subtract(BigDecimal.ONE)
                        .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
        returns.add(new ProviderReturnData("SINCE_INCEPTION", sinceInceptionPct, sinceInceptionAnnualized, today));
        return ProviderResponse.ok(returns, getProviderName());
    }

    @Override
    public ProviderResponse<List<ProviderFundData>> syncFunds() {
        LocalDate today = LocalDate.now();
        return ProviderResponse.ok(CATALOG.stream().map(f -> toFundData(f, today)).toList(), getProviderName());
    }

    private BigDecimal annualizedReturn(BigDecimal startNav, BigDecimal endNav, long days) {
        double ratio = endNav.divide(startNav, 12, RoundingMode.HALF_UP).doubleValue();
        double cagr = Math.pow(ratio, 365.0 / days) - 1;
        return BigDecimal.valueOf(cagr * 100).setScale(2, RoundingMode.HALF_UP);
    }

    private Optional<FundParams> findParams(String schemeCode) {
        return CATALOG.stream().filter(f -> f.schemeCode().equals(schemeCode)).findFirst();
    }

    /** Deterministic drift + oscillation formula anchored on {@link #REFERENCE_INCEPTION}; not real market data. */
    private BigDecimal navOn(FundParams f, LocalDate date) {
        long dayOffset = ChronoUnit.DAYS.between(REFERENCE_INCEPTION, date);
        double drift = Math.pow(1 + f.annualDrift() / 365.0, dayOffset);
        double wave = 1 + f.amplitude() * Math.sin(2 * Math.PI * dayOffset / f.waveDays());
        double base = 10.0 * drift * wave;
        return BigDecimal.valueOf(base).setScale(4, RoundingMode.HALF_UP);
    }

    private ProviderFundData toFundData(FundParams f, LocalDate asOf) {
        return new ProviderFundData(f.schemeCode(), f.isin(), f.amcCode(), f.amcName(), f.schemeName(), f.shortName(),
                f.category(), f.subCategory(), "DIRECT", "GROWTH", f.assetClass(),
                "Demo/development investment objective for " + f.schemeName() + ".", f.riskLevel(), f.benchmark(),
                f.expenseRatio(), f.exitLoad(), f.minimumLumpsum(), f.minimumSip(), f.baseAum(), navOn(f, asOf), asOf,
                REFERENCE_INCEPTION, f.fundManager());
    }
}
