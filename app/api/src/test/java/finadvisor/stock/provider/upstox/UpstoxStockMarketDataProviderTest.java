package finadvisor.stock.provider.upstox;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import finadvisor.stock.provider.MarketQuote;
import finadvisor.stock.provider.MarketStatusInfo;
import finadvisor.stock.provider.ProviderResponse;
import finadvisor.stock.provider.ProviderStockDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpstoxStockMarketDataProviderTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Mock
    private UpstoxApiClient apiClient;

    private UpstoxProperties properties;
    private UpstoxStockMarketDataProvider provider;

    @BeforeEach
    void setUp() {
        properties = new UpstoxProperties();
        properties.setTrackedSymbols(List.of("AAA", "BBB"));
        provider = new UpstoxStockMarketDataProvider(apiClient, properties);
    }

    @Test
    void searchStocks_shouldMapInstrumentSearchResultsIntoProviderStockDetails() throws Exception {
        JsonNode data = MAPPER.readTree("""
                [{"name":"RELIANCE INDUSTRIES LTD","segment":"NSE_EQ","exchange":"NSE","isin":"INE002A01018",
                  "instrument_key":"NSE_EQ|INE002A01018","trading_symbol":"RELIANCE","instrument_type":"EQ","lot_size":1}]
                """);
        when(apiClient.getData(eq("SEARCH_INSTRUMENTS"), anyString())).thenReturn(data);

        ProviderResponse<List<ProviderStockDetails>> response = provider.searchStocks("RELIANCE");

        assertThat(response.success()).isTrue();
        assertThat(response.data()).hasSize(1);
        ProviderStockDetails details = response.data().get(0);
        assertThat(details.symbol()).isEqualTo("RELIANCE");
        assertThat(details.instrumentKey()).isEqualTo("NSE_EQ|INE002A01018");
        assertThat(details.isin()).isEqualTo("INE002A01018");
        assertThat(details.companyName()).isEqualTo("RELIANCE INDUSTRIES LTD");
    }

    @Test
    void getCurrentPrice_shouldMarkRecentQuoteAsLiveRealTimeAndComputeChangePercent() throws Exception {
        // No tracked symbols configured for this scenario, so resolution goes straight to the live fallback search.
        properties.setTrackedSymbols(List.of());
        // Symbol resolution falls back to a live instrument search since the tracked cache is empty.
        JsonNode searchData = MAPPER.readTree("""
                [{"name":"RELIANCE INDUSTRIES LTD","exchange":"NSE","isin":"INE002A01018",
                  "instrument_key":"NSE_EQ|INE002A01018","trading_symbol":"RELIANCE"}]
                """);
        when(apiClient.getData(eq("SEARCH_INSTRUMENTS"), anyString())).thenReturn(searchData);

        String recentTimestamp = OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(2).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        JsonNode quoteData = MAPPER.readTree("""
                {"NSE_EQ:RELIANCE":{"ohlc":{"open":2500.0,"high":2550.0,"low":2490.0,"close":2530.0,"volume":1000,"ts":1},
                  "timestamp":"%s","instrument_token":"NSE_EQ|INE002A01018","symbol":"RELIANCE","last_price":2540.5,
                  "volume":123456,"net_change":10.5,"prev_close_price":2530.0}}
                """.formatted(recentTimestamp));
        when(apiClient.getData(eq("GET_QUOTE"), anyString())).thenReturn(quoteData);

        ProviderResponse<MarketQuote> response = provider.getCurrentPrice("RELIANCE");

        assertThat(response.success()).isTrue();
        MarketQuote quote = response.data();
        assertThat(quote.lastTradedPrice()).isEqualByComparingTo(new BigDecimal("2540.5"));
        assertThat(quote.changePercent()).isEqualByComparingTo(new BigDecimal("0.42"));
        assertThat(quote.freshness()).isEqualTo(finadvisor.marketdata.DataFreshness.LIVE);
        assertThat(quote.dataType()).isEqualTo(finadvisor.marketdata.DataType.REAL_TIME);
        assertThat(quote.source()).isEqualTo("UPSTOX");
    }

    @Test
    void getMarketStatus_shouldMapUpstoxExchangeStatusResponse() throws Exception {
        JsonNode data = MAPPER.readTree("""
                {"exchange":"NSE","status":"NORMAL_OPEN","last_updated":1705549500000}
                """);
        when(apiClient.getData(eq("GET_MARKET_STATUS"), anyString())).thenReturn(data);

        ProviderResponse<MarketStatusInfo> response = provider.getMarketStatus("NSE");

        assertThat(response.success()).isTrue();
        assertThat(response.data().status()).isEqualTo("NORMAL_OPEN");
        assertThat(response.data().exchange()).isEqualTo("NSE");
    }

    @Test
    void getTopGainers_and_getTopLosers_shouldSortByChangePercentInOppositeDirections() throws Exception {
        JsonNode instruments = MAPPER.readTree("""
                [{"name":"Company AAA","exchange":"NSE","isin":"INE111","instrument_key":"NSE_EQ|INE111","trading_symbol":"AAA"},
                 {"name":"Company BBB","exchange":"NSE","isin":"INE222","instrument_key":"NSE_EQ|INE222","trading_symbol":"BBB"}]
                """);
        when(apiClient.getData(eq("SYNC_INSTRUMENT"), anyString())).thenReturn(instruments);

        JsonNode quotes = MAPPER.readTree("""
                {"NSE_EQ:AAA":{"symbol":"AAA","last_price":110,"net_change":10,"prev_close_price":100,"ohlc":{}},
                 "NSE_EQ:BBB":{"symbol":"BBB","last_price":90,"net_change":-10,"prev_close_price":100,"ohlc":{}}}
                """);
        when(apiClient.getData(eq("GET_MOVERS"), anyString())).thenReturn(quotes);

        List<MarketQuote> gainers = provider.getTopGainers().data();
        List<MarketQuote> losers = provider.getTopLosers().data();

        assertThat(gainers.get(0).symbol()).isEqualTo("AAA");
        assertThat(losers.get(0).symbol()).isEqualTo("BBB");
    }
}
