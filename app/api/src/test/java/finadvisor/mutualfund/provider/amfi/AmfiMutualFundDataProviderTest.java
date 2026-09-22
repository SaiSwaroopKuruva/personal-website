package finadvisor.mutualfund.provider.amfi;

import finadvisor.marketdata.ProviderHealthTracker;
import finadvisor.mutualfund.provider.ProviderFundData;
import finadvisor.mutualfund.provider.ProviderResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@ExtendWith(MockitoExtension.class)
class AmfiMutualFundDataProviderTest {

    private static final String SAMPLE = String.join("\n",
            "Scheme Code;ISIN Div Payout/ ISIN Growth;ISIN Div Reinvestment;Scheme Name;Net Asset Value;Date",
            "",
            "Open Ended Schemes(Equity Scheme - Large Cap Fund)",
            "",
            "Nilgiri Mutual Fund",
            "999001;INE000A01011;-;Nilgiri Bluechip Equity Fund - Direct Plan-Growth;145.6700;22-Sep-2026");

    @Mock
    private ProviderHealthTracker healthTracker;

    private AmfiMutualFundDataProvider provider;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = org.mockito.Mockito.mock(RestClient.Builder.class, withSettings().defaultAnswer(RETURNS_DEEP_STUBS));
        RestClient restClient = org.mockito.Mockito.mock(RestClient.class, withSettings().defaultAnswer(RETURNS_DEEP_STUBS));
        when(builder.build()).thenReturn(restClient);
        org.mockito.Mockito.lenient()
                .when(restClient.get().uri(org.mockito.ArgumentMatchers.anyString()).retrieve().body(String.class))
                .thenReturn(SAMPLE);

        AmfiProperties properties = new AmfiProperties();
        provider = new AmfiMutualFundDataProvider(builder, properties, healthTracker);
    }

    @Test
    void syncFunds_shouldMapAmfiRowsIntoProviderFundDataWithInferredFields() {
        ProviderResponse<List<ProviderFundData>> response = provider.syncFunds();

        assertThat(response.success()).isTrue();
        assertThat(response.data()).hasSize(1);
        ProviderFundData fund = response.data().get(0);

        assertThat(fund.schemeCode()).isEqualTo("999001");
        assertThat(fund.isin()).isEqualTo("INE000A01011");
        assertThat(fund.amcName()).isEqualTo("Nilgiri Mutual Fund");
        assertThat(fund.amcCode()).isEqualTo("NILGIRI_MUTUAL_FUND");
        assertThat(fund.category()).isEqualTo("Open Ended Schemes");
        assertThat(fund.subCategory()).isEqualTo("Equity Scheme - Large Cap Fund");
        assertThat(fund.assetClass()).isEqualTo("EQUITY");
        assertThat(fund.planType()).isEqualTo("DIRECT");
        assertThat(fund.optionType()).isEqualTo("GROWTH");
        assertThat(fund.riskLevel()).isEqualTo("NOT_RATED");
        assertThat(fund.nav()).isEqualByComparingTo(new BigDecimal("145.6700"));
        assertThat(fund.navDate()).isEqualTo(LocalDate.of(2026, 9, 22));
        // Fields AMFI does not publish must stay null - never fabricated.
        assertThat(fund.expenseRatio()).isNull();
        assertThat(fund.aum()).isNull();
        assertThat(fund.fundManager()).isNull();
    }

    @Test
    void getHistoricalNav_shouldReportProviderLimitationInsteadOfGuessing() {
        var response = provider.getHistoricalNav("999001", LocalDate.now().minusDays(30), LocalDate.now());

        assertThat(response.success()).isFalse();
        assertThat(response.errorMessage()).contains("AMFI");
    }
}
