package finadvisor.networth.controller;

import finadvisor.networth.dto.AssetRequest;
import finadvisor.networth.dto.AssetResponse;
import finadvisor.networth.dto.LiabilityRequest;
import finadvisor.networth.dto.LiabilityResponse;
import finadvisor.networth.dto.NetWorthHistoryResponse;
import finadvisor.networth.dto.NetWorthSummaryResponse;
import finadvisor.networth.service.NetWorthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Manual net-worth tracking (Part 15/18) - always private to the authenticated user (Part 19). */
@RestController
@RequestMapping("/api/net-worth")
@RequiredArgsConstructor
@Tag(name = "Net Worth", description = "Manual assets/liabilities and net-worth aggregation across tracked investments")
public class NetWorthController {

    private final NetWorthService netWorthService;

    @GetMapping("/summary")
    @Operation(summary = "Total assets, liabilities and net worth")
    public ResponseEntity<NetWorthSummaryResponse> getSummary(Authentication auth) {
        return ResponseEntity.ok(netWorthService.getSummary(auth.getName()));
    }

    @GetMapping("/history")
    @Operation(summary = "Historical net-worth trend derived from portfolio valuation snapshots")
    public ResponseEntity<NetWorthHistoryResponse> getHistory(Authentication auth) {
        return ResponseEntity.ok(netWorthService.getHistory(auth.getName()));
    }

    @GetMapping("/assets")
    @Operation(summary = "List manually tracked assets")
    public ResponseEntity<List<AssetResponse>> listAssets(Authentication auth) {
        return ResponseEntity.ok(netWorthService.listAssets(auth.getName()));
    }

    @PostMapping("/assets")
    @Operation(summary = "Add a manually tracked asset")
    public ResponseEntity<AssetResponse> createAsset(Authentication auth, @Valid @RequestBody AssetRequest request) {
        return ResponseEntity.ok(netWorthService.createAsset(auth.getName(), request));
    }

    @PutMapping("/assets/{assetId}")
    @Operation(summary = "Update a manually tracked asset")
    public ResponseEntity<AssetResponse> updateAsset(Authentication auth, @PathVariable UUID assetId, @Valid @RequestBody AssetRequest request) {
        return ResponseEntity.ok(netWorthService.updateAsset(auth.getName(), assetId, request));
    }

    @DeleteMapping("/assets/{assetId}")
    @Operation(summary = "Remove a manually tracked asset")
    public ResponseEntity<Void> deleteAsset(Authentication auth, @PathVariable UUID assetId) {
        netWorthService.deleteAsset(auth.getName(), assetId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/liabilities")
    @Operation(summary = "List manually tracked liabilities")
    public ResponseEntity<List<LiabilityResponse>> listLiabilities(Authentication auth) {
        return ResponseEntity.ok(netWorthService.listLiabilities(auth.getName()));
    }

    @PostMapping("/liabilities")
    @Operation(summary = "Add a manually tracked liability")
    public ResponseEntity<LiabilityResponse> createLiability(Authentication auth, @Valid @RequestBody LiabilityRequest request) {
        return ResponseEntity.ok(netWorthService.createLiability(auth.getName(), request));
    }

    @PutMapping("/liabilities/{liabilityId}")
    @Operation(summary = "Update a manually tracked liability")
    public ResponseEntity<LiabilityResponse> updateLiability(Authentication auth, @PathVariable UUID liabilityId,
                                                               @Valid @RequestBody LiabilityRequest request) {
        return ResponseEntity.ok(netWorthService.updateLiability(auth.getName(), liabilityId, request));
    }

    @DeleteMapping("/liabilities/{liabilityId}")
    @Operation(summary = "Remove a manually tracked liability")
    public ResponseEntity<Void> deleteLiability(Authentication auth, @PathVariable UUID liabilityId) {
        netWorthService.deleteLiability(auth.getName(), liabilityId);
        return ResponseEntity.noContent().build();
    }
}
