package finadvisor.stock.controller;

import finadvisor.stock.dto.MarketIndexResponse;
import finadvisor.stock.dto.MarketStatusResponse;
import finadvisor.stock.dto.StockQuoteResponse;
import finadvisor.stock.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Market-wide data: indices, movers and exchange status (Part 18). */
@RestController
@RequestMapping("/api/market")
@RequiredArgsConstructor
@Tag(name = "Market", description = "Market indices, gainers/losers/most-active and exchange status")
public class MarketController {

    private final StockService stockService;

    @GetMapping("/indices")
    @Operation(summary = "Get configured market indices (e.g. Nifty 50, Nifty Bank)")
    public ResponseEntity<List<MarketIndexResponse>> indices() {
        return ResponseEntity.ok(stockService.getMarketIndices());
    }

    @GetMapping("/gainers")
    @Operation(summary = "Top gainers", description = "Computed from real quotes across this deployment's tracked stock universe - Upstox has no dedicated screener endpoint (see docs/financial-data-providers.md).")
    public ResponseEntity<List<StockQuoteResponse>> gainers() {
        return ResponseEntity.ok(stockService.getTopGainers());
    }

    @GetMapping("/losers")
    @Operation(summary = "Top losers", description = "Computed from real quotes across this deployment's tracked stock universe - see docs/financial-data-providers.md.")
    public ResponseEntity<List<StockQuoteResponse>> losers() {
        return ResponseEntity.ok(stockService.getTopLosers());
    }

    @GetMapping("/most-active")
    @Operation(summary = "Most active by volume", description = "Computed from real quotes across this deployment's tracked stock universe - see docs/financial-data-providers.md.")
    public ResponseEntity<List<StockQuoteResponse>> mostActive() {
        return ResponseEntity.ok(stockService.getMostActive());
    }

    @GetMapping("/status")
    @Operation(summary = "Get exchange trading status")
    public ResponseEntity<MarketStatusResponse> status(@RequestParam(defaultValue = "NSE") String exchange) {
        return ResponseEntity.ok(stockService.getMarketStatus(exchange));
    }
}
