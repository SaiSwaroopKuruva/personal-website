package finadvisor.mutualfund.provider.amfi;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class AmfiNavParserTest {

    private static final String SAMPLE = String.join("\n",
            "Scheme Code;ISIN Div Payout/ ISIN Growth;ISIN Div Reinvestment;Scheme Name;Net Asset Value;Date",
            "",
            "Open Ended Schemes(Debt Scheme - Overnight Fund)",
            "",
            "Axis Mutual Fund",
            "125497;INF846K01EW2;-;Axis Overnight Fund - Direct Plan-Growth;1234.5678;22-Sep-2026",
            "125498;INF846K01EX0;-;Axis Overnight Fund - Regular Plan-Growth;1230.1234;22-Sep-2026",
            "",
            "Open Ended Schemes(Equity Scheme - Large Cap Fund)",
            "",
            "Nilgiri Mutual Fund",
            "999001;INE000A01011;-;Nilgiri Bluechip Equity Fund - Direct Plan-Growth;145.6700;22-Sep-2026",
            "999002;-;-;Nilgiri Bluechip Equity Fund - Regular Plan-IDCW;98.1200;22-Sep-2026",
            // Malformed rows that must be skipped rather than fail the whole parse:
            "999003;INE000A01099;-;Fund With Invalid NAV;N.A.;22-Sep-2026",
            "999004;INE000A01100;-;Fund With Bad Date;100.00;not-a-date",
            "999005;INE000A01111;-;Fund With Negative NAV;-5.00;22-Sep-2026"
    );

    @Test
    void parse_shouldExtractValidRecordsAndSkipMalformedOnes() {
        AmfiNavParser.ParseResult result = AmfiNavParser.parse(SAMPLE);

        assertThat(result.records()).hasSize(4);
        assertThat(result.skippedLines()).isEqualTo(3);
    }

    @Test
    void parse_shouldAssociateEachRecordWithItsAmcAndCategoryContext() {
        AmfiNavParser.ParseResult result = AmfiNavParser.parse(SAMPLE);

        AmfiSchemeRecord axisOvernight = result.records().stream()
                .filter(r -> r.schemeCode().equals("125497")).findFirst().orElseThrow();
        assertThat(axisOvernight.amcName()).isEqualTo("Axis Mutual Fund");
        assertThat(axisOvernight.schemeCategoryLine()).isEqualTo("Open Ended Schemes(Debt Scheme - Overnight Fund)");
        assertThat(axisOvernight.nav()).isEqualByComparingTo(new BigDecimal("1234.5678"));
        assertThat(axisOvernight.navDate()).isEqualTo(LocalDate.of(2026, 9, 22));
        assertThat(axisOvernight.isin()).isEqualTo("INF846K01EW2");

        AmfiSchemeRecord nilgiriBluechip = result.records().stream()
                .filter(r -> r.schemeCode().equals("999002")).findFirst().orElseThrow();
        assertThat(nilgiriBluechip.amcName()).isEqualTo("Nilgiri Mutual Fund");
        // ISIN Growth column was "-" (blank) - must map to null, never the literal dash.
        assertThat(nilgiriBluechip.isin()).isNull();
    }

    @Test
    void parse_shouldReturnEmptyResultForBlankInput() {
        assertThat(AmfiNavParser.parse(null).records()).isEmpty();
        assertThat(AmfiNavParser.parse("   ").records()).isEmpty();
    }
}
