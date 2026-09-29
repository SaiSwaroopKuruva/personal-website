package finadvisor.portfolio.controller;

import finadvisor.dto.PageResponse;
import finadvisor.portfolio.dto.AllocationResponse;
import finadvisor.portfolio.dto.CreatePortfolioRequest;
import finadvisor.portfolio.dto.PerformanceResponse;
import finadvisor.portfolio.dto.PortfolioHoldingsResponse;
import finadvisor.portfolio.dto.PortfolioResponse;
import finadvisor.portfolio.dto.PortfolioSummaryResponse;
import finadvisor.portfolio.dto.TransactionRequest;
import finadvisor.portfolio.dto.TransactionResponse;
import finadvisor.portfolio.dto.UpdatePortfolioRequest;
import finadvisor.portfolio.dto.XirrResponse;
import finadvisor.portfolio.entity.AssetType;
import finadvisor.portfolio.entity.TransactionType;
import finadvisor.portfolio.service.PortfolioAnalyticsService;
import finadvisor.portfolio.service.PortfolioExportService;
import finadvisor.portfolio.service.PortfolioService;
import finadvisor.portfolio.service.PortfolioTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Portfolio, holdings, transactions, analytics and export APIs (Part 18). All data is private to the authenticated user (Part 19). */
@RestController
@RequestMapping("/api/portfolios")
@RequiredArgsConstructor
@Validated
@Tag(name = "Portfolios", description = "Portfolio tracking: transactions, derived holdings, valuation, allocation, performance and XIRR")
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final PortfolioTransactionService transactionService;
    private final PortfolioAnalyticsService analyticsService;
    private final PortfolioExportService exportService;

    @PostMapping
    @Operation(summary = "Create a portfolio")
    public ResponseEntity<PortfolioResponse> createPortfolio(Authentication auth, @Valid @RequestBody CreatePortfolioRequest request) {
        return ResponseEntity.ok(portfolioService.createPortfolio(auth.getName(), request));
    }

    @GetMapping
    @Operation(summary = "List the current user's portfolios")
    public ResponseEntity<List<PortfolioResponse>> listPortfolios(Authentication auth) {
        return ResponseEntity.ok(portfolioService.listPortfolios(auth.getName()));
    }

    @GetMapping("/{portfolioId}")
    @Operation(summary = "Get a single portfolio")
    public ResponseEntity<PortfolioResponse> getPortfolio(Authentication auth, @PathVariable UUID portfolioId) {
        return ResponseEntity.ok(portfolioService.getPortfolio(auth.getName(), portfolioId));
    }

    @PutMapping("/{portfolioId}")
    @Operation(summary = "Update a portfolio")
    public ResponseEntity<PortfolioResponse> updatePortfolio(Authentication auth, @PathVariable UUID portfolioId,
                                                              @Valid @RequestBody UpdatePortfolioRequest request) {
        return ResponseEntity.ok(portfolioService.updatePortfolio(auth.getName(), portfolioId, request));
    }

    @DeleteMapping("/{portfolioId}")
    @Operation(summary = "Archive a portfolio")
    public ResponseEntity<Void> deletePortfolio(Authentication auth, @PathVariable UUID portfolioId) {
        portfolioService.deletePortfolio(auth.getName(), portfolioId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{portfolioId}/summary")
    @Operation(summary = "Portfolio overview metrics", description = "Never equates cumulative profit with XIRR (Part 8/13).")
    public ResponseEntity<PortfolioSummaryResponse> getSummary(Authentication auth, @PathVariable UUID portfolioId) {
        return ResponseEntity.ok(analyticsService.getSummary(auth.getName(), portfolioId));
    }

    @GetMapping("/{portfolioId}/holdings")
    @Operation(summary = "Derived holdings with live valuation", description = "Missing valuations are null, never zero (Part 9).")
    public ResponseEntity<PortfolioHoldingsResponse> getHoldings(Authentication auth, @PathVariable UUID portfolioId) {
        return ResponseEntity.ok(analyticsService.getHoldings(auth.getName(), portfolioId));
    }

    @GetMapping("/{portfolioId}/allocation")
    @Operation(summary = "Asset allocation by asset class and holding")
    public ResponseEntity<AllocationResponse> getAllocation(Authentication auth, @PathVariable UUID portfolioId) {
        return ResponseEntity.ok(analyticsService.getAllocation(auth.getName(), portfolioId));
    }

    @GetMapping("/{portfolioId}/performance")
    @Operation(summary = "Portfolio value/gain-loss over time", description = "Built from valuation snapshots; range may be limited by available history (Part 11).")
    public ResponseEntity<PerformanceResponse> getPerformance(Authentication auth, @PathVariable UUID portfolioId,
                                                               @RequestParam(defaultValue = "ALL") String range) {
        return ResponseEntity.ok(analyticsService.getPerformance(auth.getName(), portfolioId, range));
    }

    @GetMapping("/{portfolioId}/xirr")
    @Operation(summary = "Annualized money-weighted return", description = "Returns a structured status instead of a misleading percentage when undefined (Part 12).")
    public ResponseEntity<XirrResponse> getXirr(Authentication auth, @PathVariable UUID portfolioId) {
        return ResponseEntity.ok(analyticsService.getXirr(auth.getName(), portfolioId));
    }

    @GetMapping("/{portfolioId}/transactions")
    @Operation(summary = "List transactions with filters, sorting and pagination")
    public ResponseEntity<PageResponse<TransactionResponse>> listTransactions(
            Authentication auth, @PathVariable UUID portfolioId,
            @RequestParam(required = false) AssetType assetType,
            @RequestParam(required = false) String symbol,
            @RequestParam(required = false) TransactionType transactionType,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(transactionService.listTransactions(auth.getName(), portfolioId, assetType, symbol,
                transactionType, dateFrom, dateTo, page, size));
    }

    @PostMapping("/{portfolioId}/transactions")
    @Operation(summary = "Record a transaction (buy/sell/purchase/redemption/dividend/fee/tax/adjustment)")
    public ResponseEntity<TransactionResponse> createTransaction(Authentication auth, @PathVariable UUID portfolioId,
                                                                   @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(transactionService.createTransaction(auth.getName(), portfolioId, request));
    }

    @GetMapping("/{portfolioId}/transactions/{transactionId}")
    @Operation(summary = "Get a single transaction")
    public ResponseEntity<TransactionResponse> getTransaction(Authentication auth, @PathVariable UUID portfolioId,
                                                                @PathVariable UUID transactionId) {
        return ResponseEntity.ok(transactionService.getTransaction(auth.getName(), portfolioId, transactionId));
    }

    @PutMapping("/{portfolioId}/transactions/{transactionId}")
    @Operation(summary = "Edit a transaction", description = "Recalculates holdings/gains and invalidates affected snapshots (Part 7).")
    public ResponseEntity<TransactionResponse> updateTransaction(Authentication auth, @PathVariable UUID portfolioId,
                                                                   @PathVariable UUID transactionId,
                                                                   @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(transactionService.updateTransaction(auth.getName(), portfolioId, transactionId, request));
    }

    @DeleteMapping("/{portfolioId}/transactions/{transactionId}")
    @Operation(summary = "Delete a transaction", description = "Recalculates holdings/gains and invalidates affected snapshots (Part 7).")
    public ResponseEntity<Void> deleteTransaction(Authentication auth, @PathVariable UUID portfolioId, @PathVariable UUID transactionId) {
        transactionService.deleteTransaction(auth.getName(), portfolioId, transactionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{portfolioId}/export/holdings")
    @Operation(summary = "Export holdings as CSV")
    public ResponseEntity<String> exportHoldings(Authentication auth, @PathVariable UUID portfolioId) {
        return csvResponse(exportService.exportHoldingsCsv(auth.getName(), portfolioId), "holdings.csv");
    }

    @GetMapping("/{portfolioId}/export/transactions")
    @Operation(summary = "Export transactions as CSV")
    public ResponseEntity<String> exportTransactions(Authentication auth, @PathVariable UUID portfolioId) {
        return csvResponse(exportService.exportTransactionsCsv(auth.getName(), portfolioId), "transactions.csv");
    }

    @GetMapping("/{portfolioId}/export/summary")
    @Operation(summary = "Export portfolio summary as CSV", description = "Not a tax statement or official brokerage statement (Part 17).")
    public ResponseEntity<String> exportSummary(Authentication auth, @PathVariable UUID portfolioId) {
        return csvResponse(exportService.exportSummaryCsv(auth.getName(), portfolioId), "portfolio-summary.csv");
    }

    private ResponseEntity<String> csvResponse(String csv, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build());
        headers.setContentType(MediaType.parseMediaType("text/csv;charset=UTF-8"));
        return ResponseEntity.ok().headers(headers).body(csv);
    }
}
