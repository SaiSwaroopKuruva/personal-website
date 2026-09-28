package finadvisor.stock.service.impl;

import finadvisor.stock.dto.StockComparisonItemResponse;
import finadvisor.stock.dto.StockComparisonResponse;
import finadvisor.stock.dto.StockDetailsResponse;
import finadvisor.stock.dto.StockQuoteResponse;
import finadvisor.stock.exception.InvalidStockComparisonRequestException;
import finadvisor.stock.exception.StockNotFoundException;
import finadvisor.stock.service.StockComparisonService;
import finadvisor.stock.service.StockService;
import finadvisor.stock.util.StockDisclaimers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockComparisonServiceImpl implements StockComparisonService {

    private static final int MIN_STOCKS_TO_COMPARE = 2;
    private static final int MAX_STOCKS_TO_COMPARE = 4;
    private static final String LIMITATION_NOTE =
            "Key ratios (PE, PB, EPS, ROE, ROCE, debt-to-equity, dividend yield, 52-week range) are not shown - "
                    + "this deployment's provider integration does not yet expose a verified fundamentals source.";

    private final StockService stockService;

    @Override
    public StockComparisonResponse compare(List<String> symbols) {
        List<String> distinctSymbols = symbols.stream().map(String::strip).filter(s -> !s.isEmpty()).distinct().toList();
        if (distinctSymbols.size() < MIN_STOCKS_TO_COMPARE) {
            throw new InvalidStockComparisonRequestException("Provide at least " + MIN_STOCKS_TO_COMPARE + " distinct stock symbols to compare");
        }
        if (distinctSymbols.size() > MAX_STOCKS_TO_COMPARE) {
            throw new InvalidStockComparisonRequestException("You can compare at most " + MAX_STOCKS_TO_COMPARE + " stocks at a time");
        }

        List<StockComparisonItemResponse> items = distinctSymbols.stream().map(symbol -> {
            StockDetailsResponse details;
            try {
                details = stockService.getDetails(symbol);
            } catch (StockNotFoundException ex) {
                throw new InvalidStockComparisonRequestException("Unknown stock symbol: " + symbol);
            }
            StockQuoteResponse quote = fetchQuoteBestEffort(symbol);
            return new StockComparisonItemResponse(details.symbol(), details.companyName(), details.exchange(),
                    details.sector(), details.series(), details.lotSize(), quote);
        }).toList();

        return new StockComparisonResponse(items, StockDisclaimers.COMPARISON, LIMITATION_NOTE);
    }

    private StockQuoteResponse fetchQuoteBestEffort(String symbol) {
        try {
            return stockService.getCurrentPrice(symbol);
        } catch (RuntimeException ex) {
            return null;
        }
    }
}
