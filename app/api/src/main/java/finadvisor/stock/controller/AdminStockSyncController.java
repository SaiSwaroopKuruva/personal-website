package finadvisor.stock.controller;

import finadvisor.stock.dto.StockSyncResultResponse;
import finadvisor.stock.service.StockDataSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Admin-triggered stock data synchronization. Scheduled sync is disabled by default. */
@RestController
@RequestMapping("/api/admin/stocks")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Stock Sync", description = "Manually trigger stock market-data provider synchronization")
public class AdminStockSyncController {

    private final StockDataSyncService syncService;

    @PostMapping("/sync")
    @Operation(summary = "Run the full stock sync pipeline (tracked instruments + end-of-day prices)")
    public ResponseEntity<List<StockSyncResultResponse>> syncAll() {
        return ResponseEntity.ok(syncService.syncAll());
    }

    @PostMapping("/sync/instruments")
    @Operation(summary = "Sync only the tracked instrument catalog")
    public ResponseEntity<StockSyncResultResponse> syncInstruments() {
        return ResponseEntity.ok(syncService.syncInstruments());
    }

    @PostMapping("/sync/eod")
    @Operation(summary = "Sync only end-of-day OHLC prices for tracked instruments")
    public ResponseEntity<StockSyncResultResponse> syncEod() {
        return ResponseEntity.ok(syncService.syncEndOfDayPrices());
    }
}
