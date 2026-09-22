package finadvisor.stock.service;

import finadvisor.stock.dto.CandleHistoryResponse;
import finadvisor.stock.dto.MarketIndexResponse;
import finadvisor.stock.dto.MarketStatusResponse;
import finadvisor.stock.dto.NewsItemResponse;
import finadvisor.stock.dto.StockDetailsResponse;
import finadvisor.stock.dto.StockQuoteResponse;

import java.time.LocalDate;
import java.util.List;

public interface StockService {

    List<StockDetailsResponse> search(String query);

    StockDetailsResponse getDetails(String symbol);

    StockQuoteResponse getCurrentPrice(String symbol);

    CandleHistoryResponse getHistoricalPrices(String symbol, LocalDate from, LocalDate to);

    CandleHistoryResponse getIntradayPrices(String symbol);

    List<MarketIndexResponse> getMarketIndices();

    List<StockQuoteResponse> getTopGainers();

    List<StockQuoteResponse> getTopLosers();

    List<StockQuoteResponse> getMostActive();

    MarketStatusResponse getMarketStatus(String exchange);

    List<NewsItemResponse> getNews(String symbol);
}
