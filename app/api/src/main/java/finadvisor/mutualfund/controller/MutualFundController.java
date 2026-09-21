package finadvisor.mutualfund.controller;

import finadvisor.dto.PageResponse;
import finadvisor.mutualfund.dto.ComparisonResponse;
import finadvisor.mutualfund.dto.FilterMetadataResponse;
import finadvisor.mutualfund.dto.FundManagerResponse;
import finadvisor.mutualfund.dto.HoldingResponse;
import finadvisor.mutualfund.dto.MutualFundDetailsResponse;
import finadvisor.mutualfund.dto.MutualFundSummaryResponse;
import finadvisor.mutualfund.dto.NavHistoryResponse;
import finadvisor.mutualfund.dto.ReturnsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import finadvisor.mutualfund.service.MutualFundComparisonService;
import finadvisor.mutualfund.service.MutualFundService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/mutual-funds")
@RequiredArgsConstructor
@Validated
@Tag(name = "Mutual Funds", description = "Mutual fund discovery, details, NAV history, returns and holdings")
public class MutualFundController {

    private static final String SCHEME_CODE_PATTERN = "^[A-Za-z0-9._-]{1,50}$";

    private final MutualFundService mutualFundService;
    private final MutualFundComparisonService comparisonService;

    @GetMapping
    @Operation(summary = "Search and filter mutual funds", description = "Public endpoint; returns a paginated, factual list of mutual fund schemes.")
    public ResponseEntity<PageResponse<MutualFundSummaryResponse>> search(
            Authentication authentication,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String amc,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String subCategory,
            @RequestParam(required = false) String planType,
            @RequestParam(required = false) String optionType,
            @RequestParam(required = false) String riskLevel,
            @RequestParam(required = false) BigDecimal minAum,
            @RequestParam(required = false) BigDecimal maxAum,
            @RequestParam(required = false) BigDecimal minExpenseRatio,
            @RequestParam(required = false) BigDecimal maxExpenseRatio,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "ASC") String direction,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(mutualFundService.search(search, amc, category, subCategory, planType, optionType,
                riskLevel, minAum, maxAum, minExpenseRatio, maxExpenseRatio, sort, direction, page, size,
                currentEmail(authentication)));
    }

    @GetMapping("/filters")
    @Operation(summary = "Get filter metadata (AMCs, categories, risk levels, etc.) for the explorer UI")
    public ResponseEntity<FilterMetadataResponse> filters() {
        return ResponseEntity.ok(mutualFundService.getFilterMetadata());
    }

    @GetMapping("/compare")
    @Operation(summary = "Compare 2-4 mutual fund schemes side by side", description = "Presents factual metrics only; not a ranking or recommendation.")
    public ResponseEntity<ComparisonResponse> compare(
            @RequestParam @Parameter(description = "Comma-separated scheme codes, e.g. DEMO001,DEMO002") String schemes) {
        java.util.List<String> schemeCodes = java.util.Arrays.stream(schemes.split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).toList();
        return ResponseEntity.ok(comparisonService.compare(schemeCodes));
    }

    @GetMapping("/{schemeCode}")
    @Operation(summary = "Get full details for a single mutual fund scheme")
    public ResponseEntity<MutualFundDetailsResponse> getDetails(
            Authentication authentication,
            @PathVariable @Pattern(regexp = SCHEME_CODE_PATTERN) String schemeCode) {
        return ResponseEntity.ok(mutualFundService.getDetails(schemeCode, currentEmail(authentication)));
    }

    @GetMapping("/{schemeCode}/nav-history")
    @Operation(summary = "Get historical NAV points for a scheme", description = "Defaults to the last 1 year if 'from'/'to' are omitted.")
    public ResponseEntity<NavHistoryResponse> getNavHistory(
            @PathVariable @Pattern(regexp = SCHEME_CODE_PATTERN) String schemeCode,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) @Parameter(description = "DAILY, WEEKLY or MONTHLY") String interval) {
        return ResponseEntity.ok(mutualFundService.getNavHistory(schemeCode, from, to, interval));
    }

    @GetMapping("/{schemeCode}/returns")
    @Operation(summary = "Get trailing returns for a scheme (absolute and annualized, clearly labeled)")
    public ResponseEntity<ReturnsResponse> getReturns(@PathVariable @Pattern(regexp = SCHEME_CODE_PATTERN) String schemeCode) {
        return ResponseEntity.ok(mutualFundService.getReturns(schemeCode));
    }

    @GetMapping("/{schemeCode}/holdings")
    @Operation(summary = "Get portfolio holdings for a scheme as of its latest disclosed date")
    public ResponseEntity<PageResponse<HoldingResponse>> getHoldings(
            @PathVariable @Pattern(regexp = SCHEME_CODE_PATTERN) String schemeCode,
            @RequestParam(required = false) String sector,
            @RequestParam(required = false) String assetType,
            @RequestParam(required = false) LocalDate asOfDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(mutualFundService.getHoldings(schemeCode, sector, assetType, asOfDate, page, size));
    }

    @GetMapping("/{schemeCode}/managers")
    @Operation(summary = "Get fund manager information for a scheme")
    public ResponseEntity<java.util.List<FundManagerResponse>> getManagers(
            @PathVariable @Pattern(regexp = SCHEME_CODE_PATTERN) String schemeCode) {
        return ResponseEntity.ok(mutualFundService.getManagers(schemeCode));
    }

    private String currentEmail(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return authentication.getName();
    }
}
