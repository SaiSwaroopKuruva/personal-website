package finadvisor.controller;

import finadvisor.dto.mutualfund.SyncResultResponse;
import finadvisor.service.MutualFundDataSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Admin-triggered mutual fund data synchronization (Part 29). Scheduled sync is disabled by default. */
@RestController
@RequestMapping("/api/admin/mutual-funds")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Mutual Fund Sync", description = "Manually trigger mutual fund market-data provider synchronization")
public class AdminMutualFundSyncController {

    private final MutualFundDataSyncService syncService;

    @PostMapping("/sync")
    @Operation(summary = "Run the full mutual fund sync pipeline (funds, NAV, holdings, returns, managers)")
    public ResponseEntity<List<SyncResultResponse>> syncAll() {
        return ResponseEntity.ok(syncService.syncAll());
    }

    @PostMapping("/sync/nav")
    @Operation(summary = "Sync only current NAV for all active funds")
    public ResponseEntity<SyncResultResponse> syncNav() {
        return ResponseEntity.ok(syncService.syncNav());
    }
}
