package finadvisor.provider.demo;

import finadvisor.provider.ProviderHoldingData;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Static fictional top-holdings reference data for {@link DemoMutualFundDataProvider}. Demo/dev use only. */
final class DemoReferenceData {

    private static final Map<String, List<ProviderHoldingData>> HOLDINGS = Map.ofEntries(
            Map.entry("DEMO001", List.of(
                    new ProviderHoldingData("Alpha Industries Ltd", "INE100A01011", "Financial Services", "EQUITY", new BigDecimal("9.20"), new BigDecimal("1250000"), new BigDecimal("458000000.00"), LocalDate.now()),
                    new ProviderHoldingData("Beta Technologies Ltd", "INE100A01029", "Information Technology", "EQUITY", new BigDecimal("8.10"), new BigDecimal("980000"), new BigDecimal("402000000.00"), LocalDate.now()),
                    new ProviderHoldingData("Gamma Bank Ltd", "INE100A01037", "Financial Services", "EQUITY", new BigDecimal("7.40"), new BigDecimal("1100000"), new BigDecimal("367000000.00"), LocalDate.now()))),
            Map.entry("DEMO002", List.of(
                    new ProviderHoldingData("Beta Technologies Ltd", "INE100A01029", "Information Technology", "EQUITY", new BigDecimal("8.70"), new BigDecimal("760000"), new BigDecimal("318000000.00"), LocalDate.now()),
                    new ProviderHoldingData("Theta Automobiles Ltd", "INE100A01078", "Automobile", "EQUITY", new BigDecimal("7.30"), new BigDecimal("505000"), new BigDecimal("271000000.00"), LocalDate.now()))),
            Map.entry("DEMO003", List.of(
                    new ProviderHoldingData("Kappa Materials Ltd", "INE100A01094", "Materials", "EQUITY", new BigDecimal("6.90"), new BigDecimal("720000"), new BigDecimal("187000000.00"), LocalDate.now()),
                    new ProviderHoldingData("Iota Infra Ltd", "INE100A01086", "Industrials", "EQUITY", new BigDecimal("6.20"), new BigDecimal("655000"), new BigDecimal("168000000.00"), LocalDate.now()))),
            Map.entry("DEMO004", List.of(
                    new ProviderHoldingData("Xi Logistics Ltd", "INE100A01136", "Industrials", "EQUITY", new BigDecimal("5.40"), new BigDecimal("610000"), new BigDecimal("88000000.00"), LocalDate.now()))),
            Map.entry("DEMO005", List.of(
                    new ProviderHoldingData("Alpha Industries Ltd", "INE100A01011", "Financial Services", "EQUITY", new BigDecimal("8.50"), new BigDecimal("410000"), new BigDecimal("384000000.00"), LocalDate.now()))),
            Map.entry("DEMO006", List.of(
                    new ProviderHoldingData("182-Day T-Bill 2026", "IN002A01234", "Government Securities", "DEBT", new BigDecimal("18.50"), null, new BigDecimal("2930000000.00"), LocalDate.now()))),
            Map.entry("DEMO007", List.of(
                    new ProviderHoldingData("7.26% GOI 2033", "IN0020230012", "Government Securities", "DEBT", new BigDecimal("15.60"), null, new BigDecimal("683000000.00"), LocalDate.now()))),
            Map.entry("DEMO008", List.of(
                    new ProviderHoldingData("Alpha Industries Ltd", "INE100A01011", "Financial Services", "EQUITY", new BigDecimal("6.10"), new BigDecimal("320000"), new BigDecimal("467000000.00"), LocalDate.now()),
                    new ProviderHoldingData("7.26% GOI 2033", "IN0020230012", "Government Securities", "DEBT", new BigDecimal("14.20"), null, new BigDecimal("1087000000.00"), LocalDate.now()))),
            Map.entry("DEMO009", List.of(
                    new ProviderHoldingData("Gamma Bank Ltd", "INE100A01037", "Financial Services", "EQUITY", new BigDecimal("7.80"), new BigDecimal("360000"), new BigDecimal("408000000.00"), LocalDate.now()))),
            Map.entry("DEMO010", List.of(
                    new ProviderHoldingData("Alpha Industries Ltd", "INE100A01011", "Financial Services", "EQUITY", new BigDecimal("10.80"), new BigDecimal("3250000"), new BigDecimal("322000000.00"), LocalDate.now()),
                    new ProviderHoldingData("Beta Technologies Ltd", "INE100A01029", "Information Technology", "EQUITY", new BigDecimal("9.40"), new BigDecimal("2810000"), new BigDecimal("280000000.00"), LocalDate.now()))));

    private DemoReferenceData() {
    }

    static List<ProviderHoldingData> holdingsFor(String schemeCode) {
        return HOLDINGS.getOrDefault(schemeCode, List.of());
    }
}
