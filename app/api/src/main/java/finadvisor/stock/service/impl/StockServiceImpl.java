package finadvisor.stock.service.impl;

import finadvisor.marketdata.ErrorCategory;
import finadvisor.stock.dto.CandleHistoryResponse;
import finadvisor.stock.dto.CandleResponse;
import finadvisor.stock.dto.MarketIndexResponse;
import finadvisor.stock.dto.MarketStatusResponse;
import finadvisor.stock.dto.NewsItemResponse;
import finadvisor.stock.dto.StockDetailsResponse;
import finadvisor.stock.dto.StockQuoteResponse;
import finadvisor.stock.exception.StockNotFoundException;
import finadvisor.stock.provider.MarketCandle;
import finadvisor.stock.provider.MarketIndex;
import finadvisor.stock.provider.MarketQuote;
import finadvisor.stock.provider.MarketStatusInfo;
import finadvisor.stock.provider.NewsItem;
import finadvisor.stock.provider.StockProviderCallExecutor;
import finadvisor.stock.provider.ProviderResponse;
import finadvisor.stock.provider.ProviderStockDetails;
import finadvisor.stock.provider.StockMarketDataProvider;
import finadvisor.stock.provider.StockProviderException;
import finadvisor.stock.service.StockService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

@Service
public class StockServiceImpl implements StockService {

    private final Optional<StockMarketDataProvider> provider;
    private final StockProviderCallExecutor providerCallExecutor;

    public StockServiceImpl(Optional<StockMarketDataProvider> provider, StockProviderCallExecutor providerCallExecutor) {
        this.provider = provider;
        this.providerCallExecutor = providerCallExecutor;
    }

    @Override
    public List<StockDetailsResponse> search(String query) {
        return call("searchStocks", p -> p.searchStocks(query)).stream().map(this::toDetails).toList();
    }

    @Override
    public StockDetailsResponse getDetails(String symbol) {
        return toDetails(call("getStockDetails", p -> p.getStockDetails(symbol)));
    }

    @Override
    public StockQuoteResponse getCurrentPrice(String symbol) {
        return toQuote(call("getCurrentPrice", p -> p.getCurrentPrice(symbol)));
    }

    @Override
    public CandleHistoryResponse getHistoricalPrices(String symbol, LocalDate from, LocalDate to) {
        LocalDate effectiveFrom = from != null ? from : LocalDate.now().minusYears(1);
        LocalDate effectiveTo = to != null ? to : LocalDate.now();
        List<CandleResponse> candles = call("getHistoricalPrices", p -> p.getHistoricalPrices(symbol, effectiveFrom, effectiveTo))
                .stream().map(this::toCandle).toList();
        return new CandleHistoryResponse(symbol, "EOD", candles);
    }

    @Override
    public CandleHistoryResponse getIntradayPrices(String symbol) {
        List<CandleResponse> candles = call("getIntradayPrices", p -> p.getIntradayPrices(symbol)).stream().map(this::toCandle).toList();
        return new CandleHistoryResponse(symbol, "1MIN", candles);
    }

    @Override
    public List<MarketIndexResponse> getMarketIndices() {
        return call("getMarketIndices", StockMarketDataProvider::getMarketIndices).stream().map(this::toIndex).toList();
    }

    @Override
    public List<StockQuoteResponse> getTopGainers() {
        return call("getTopGainers", StockMarketDataProvider::getTopGainers).stream().map(this::toQuote).toList();
    }

    @Override
    public List<StockQuoteResponse> getTopLosers() {
        return call("getTopLosers", StockMarketDataProvider::getTopLosers).stream().map(this::toQuote).toList();
    }

    @Override
    public List<StockQuoteResponse> getMostActive() {
        return call("getMostActive", StockMarketDataProvider::getMostActive).stream().map(this::toQuote).toList();
    }

    @Override
    public MarketStatusResponse getMarketStatus(String exchange) {
        MarketStatusInfo info = call("getMarketStatus", p -> p.getMarketStatus(exchange));
        return new MarketStatusResponse(info.exchange(), info.status(), info.lastUpdated());
    }

    @Override
    public List<NewsItemResponse> getNews(String symbol) {
        return call("getNews", p -> p.getNews(symbol)).stream()
                .map(n -> new NewsItemResponse(n.heading(), n.summary(), n.articleLink(), n.publishedAt()))
                .toList();
    }

    private <T> T call(String operation, Function<StockMarketDataProvider, ProviderResponse<T>> invocation) {
        StockMarketDataProvider activeProvider = provider.orElseThrow(() -> new StockProviderException(
                "No stock market-data provider is configured (set STOCK_DATA_PROVIDER=UPSTOX)", ErrorCategory.PROVIDER_UNAVAILABLE));
        Supplier<ProviderResponse<T>> call = () -> invocation.apply(activeProvider);
        ProviderResponse<T> response = providerCallExecutor.executeWithRetry(operation, call);
        if (!response.success()) {
            throw new StockNotFoundException(response.errorMessage());
        }
        return response.data();
    }

    private StockDetailsResponse toDetails(ProviderStockDetails d) {
        return new StockDetailsResponse(d.symbol(), d.exchange(), d.isin(), d.companyName(), d.sector(), d.series(), d.lotSize());
    }

    private StockQuoteResponse toQuote(MarketQuote q) {
        return new StockQuoteResponse(q.symbol(), q.exchange(), q.lastTradedPrice(), q.previousClose(), q.open(), q.high(),
                q.low(), q.volume(), q.change(), q.changePercent(), q.timestamp(), q.source(), q.dataType(), q.freshness());
    }

    private CandleResponse toCandle(MarketCandle c) {
        return new CandleResponse(c.timestamp(), c.open(), c.high(), c.low(), c.close(), c.volume());
    }

    private MarketIndexResponse toIndex(MarketIndex i) {
        return new MarketIndexResponse(i.name(), i.lastPrice(), i.change(), i.changePercent(), i.timestamp());
    }
}
