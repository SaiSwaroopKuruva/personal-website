package finadvisor.stock.controller;

import finadvisor.stock.dto.CandleHistoryResponse;
import finadvisor.stock.dto.NewsItemResponse;
import finadvisor.stock.dto.StockDetailsResponse;
import finadvisor.stock.dto.StockQuoteResponse;
import finadvisor.stock.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/** Stock discovery, quotes and history - data integration only (Part 1: never trading/order placement). */
@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
@Validated
@Tag(name = "Stocks", description = "Stock search, details, quotes and price history (provider-backed, informational only)")
public class StockController {

    private static final String SYMBOL_PATTERN = "^[A-Za-z0-9._&-]{1,50}$";

    private final StockService stockService;

    @GetMapping
    @Operation(summary = "Search stocks by symbol or company name")
    public ResponseEntity<List<StockDetailsResponse>> search(@RequestParam String query) {
        return ResponseEntity.ok(stockService.search(query));
    }

    @GetMapping("/{symbol}")
    @Operation(summary = "Get reference details for a single stock")
    public ResponseEntity<StockDetailsResponse> getDetails(@PathVariable @Pattern(regexp = SYMBOL_PATTERN) String symbol) {
        return ResponseEntity.ok(stockService.getDetails(symbol));
    }

    @GetMapping("/{symbol}/prices")
    @Operation(summary = "Get the current market quote for a stock", description = "Response always carries source/dataType/freshness (Part 19).")
    public ResponseEntity<StockQuoteResponse> getCurrentPrice(@PathVariable @Pattern(regexp = SYMBOL_PATTERN) String symbol) {
        return ResponseEntity.ok(stockService.getCurrentPrice(symbol));
    }

    @GetMapping("/{symbol}/ohlc")
    @Operation(summary = "Get historical or intraday OHLC candles for a stock")
    public ResponseEntity<CandleHistoryResponse> getOhlc(
            @PathVariable @Pattern(regexp = SYMBOL_PATTERN) String symbol,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(defaultValue = "EOD") String interval) {
        if ("INTRADAY".equalsIgnoreCase(interval)) {
            return ResponseEntity.ok(stockService.getIntradayPrices(symbol));
        }
        return ResponseEntity.ok(stockService.getHistoricalPrices(symbol, from, to));
    }

    @GetMapping("/{symbol}/metrics")
    @Operation(summary = "Financial metrics (key ratios)", description = "Not implemented in this phase - requires the Upstox Fundamentals Key Ratios API (see docs/financial-data-providers.md).")
    public ResponseEntity<StockDetailsResponse> getMetrics(@PathVariable @Pattern(regexp = SYMBOL_PATTERN) String symbol) {
        return notImplemented();
    }

    @GetMapping("/{symbol}/financials")
    @Operation(summary = "Financial statements", description = "Not implemented in this phase - requires the Upstox Fundamentals balance sheet/income statement/cash flow APIs (see docs/financial-data-providers.md).")
    public ResponseEntity<StockDetailsResponse> getFinancials(@PathVariable @Pattern(regexp = SYMBOL_PATTERN) String symbol) {
        return notImplemented();
    }

    @GetMapping("/{symbol}/dividends")
    @Operation(summary = "Dividend history", description = "Not implemented in this phase - requires the Upstox Fundamentals Corporate Actions API (see docs/financial-data-providers.md).")
    public ResponseEntity<StockDetailsResponse> getDividends(@PathVariable @Pattern(regexp = SYMBOL_PATTERN) String symbol) {
        return notImplemented();
    }

    @GetMapping("/{symbol}/corporate-actions")
    @Operation(summary = "Corporate actions (splits, bonuses, rights)", description = "Not implemented in this phase - requires the Upstox Fundamentals Corporate Actions API (see docs/financial-data-providers.md).")
    public ResponseEntity<StockDetailsResponse> getCorporateActions(@PathVariable @Pattern(regexp = SYMBOL_PATTERN) String symbol) {
        return notImplemented();
    }

    @GetMapping("/{symbol}/news")
    @Operation(summary = "Recent news for a stock (last 7 days, per Upstox News API)")
    public ResponseEntity<List<NewsItemResponse>> getNews(@PathVariable @Pattern(regexp = SYMBOL_PATTERN) String symbol) {
        return ResponseEntity.ok(stockService.getNews(symbol));
    }

    private <T> ResponseEntity<T> notImplemented() {
        return ResponseEntity.status(501).build();
    }
}
