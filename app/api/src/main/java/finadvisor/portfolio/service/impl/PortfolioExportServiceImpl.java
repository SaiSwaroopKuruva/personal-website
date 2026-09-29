package finadvisor.portfolio.service.impl;

import finadvisor.portfolio.dto.HoldingResponse;
import finadvisor.portfolio.dto.PortfolioHoldingsResponse;
import finadvisor.portfolio.dto.PortfolioSummaryResponse;
import finadvisor.portfolio.dto.TransactionResponse;
import finadvisor.portfolio.entity.Portfolio;
import finadvisor.portfolio.service.PortfolioAnalyticsService;
import finadvisor.portfolio.service.PortfolioExportService;
import finadvisor.portfolio.service.PortfolioService;
import finadvisor.portfolio.service.PortfolioTransactionService;
import finadvisor.portfolio.util.CsvUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PortfolioExportServiceImpl implements PortfolioExportService {

    private final PortfolioService portfolioService;
    private final PortfolioTransactionService transactionService;
    private final PortfolioAnalyticsService analyticsService;

    public PortfolioExportServiceImpl(PortfolioService portfolioService, PortfolioTransactionService transactionService,
                                       PortfolioAnalyticsService analyticsService) {
        this.portfolioService = portfolioService;
        this.transactionService = transactionService;
        this.analyticsService = analyticsService;
    }

    @Override
    public String exportHoldingsCsv(String userEmail, UUID portfolioId) {
        portfolioService.requireOwnedPortfolio(userEmail, portfolioId);
        PortfolioHoldingsResponse holdings = analyticsService.getHoldings(userEmail, portfolioId);
        StringBuilder sb = new StringBuilder();
        sb.append(CsvUtil.row("Asset Type", "Symbol/Scheme", "Name", "Quantity", "Average Cost", "Cost Basis",
                "Latest Price", "Price Source", "Price Timestamp", "Valuation Status", "Market Value",
                "Unrealized Gain", "Unrealized Gain %", "Allocation %"));
        for (HoldingResponse h : holdings.holdings()) {
            sb.append(CsvUtil.row(h.assetType(), h.symbol(), h.name(), h.quantity(), h.averageCost(), h.costBasis(),
                    h.valuation() != null ? h.valuation().price() : null,
                    h.valuation() != null ? h.valuation().source() : null,
                    h.valuation() != null ? h.valuation().sourceTimestamp() : null,
                    h.valuation() != null ? h.valuation().status() : null,
                    h.marketValue(), h.unrealizedGain(), h.unrealizedGainPercent(), h.allocationPercent()));
        }
        return sb.toString();
    }

    @Override
    public String exportTransactionsCsv(String userEmail, UUID portfolioId) {
        Portfolio portfolio = portfolioService.requireOwnedPortfolio(userEmail, portfolioId);
        StringBuilder sb = new StringBuilder();
        sb.append(CsvUtil.row("Date", "Asset Type", "Symbol/Scheme", "Exchange", "Transaction Type", "Quantity",
                "Price Per Unit", "Gross Amount", "Fees", "Taxes", "Net Amount", "Notes", "External Reference"));
        for (TransactionResponse tx : transactionService.loadLedger(portfolio.getId()).stream()
                .map(this::toResponse).sorted((a, b) -> b.transactionDate().compareTo(a.transactionDate())).toList()) {
            sb.append(CsvUtil.row(tx.transactionDate(), tx.assetType(),
                    tx.assetType() == finadvisor.portfolio.entity.AssetType.STOCK ? tx.stockSymbol() : tx.mutualFundSchemeCode(),
                    tx.stockExchange(), tx.transactionType(), tx.quantity(), tx.pricePerUnit(), tx.grossAmount(),
                    tx.fees(), tx.taxes(), tx.netAmount(), tx.notes(), tx.externalReference()));
        }
        return sb.toString();
    }

    @Override
    public String exportSummaryCsv(String userEmail, UUID portfolioId) {
        PortfolioSummaryResponse summary = analyticsService.getSummary(userEmail, portfolioId);
        StringBuilder sb = new StringBuilder();
        sb.append(CsvUtil.row("Field", "Value"));
        sb.append(CsvUtil.row("Portfolio Name", summary.portfolioName()));
        sb.append(CsvUtil.row("Report Date", java.time.LocalDate.now()));
        sb.append(CsvUtil.row("Invested Amount", summary.investedAmount()));
        sb.append(CsvUtil.row("Current Market Value", summary.currentMarketValue() != null ? summary.currentMarketValue() : "UNAVAILABLE"));
        sb.append(CsvUtil.row("Realized Gain/Loss", summary.realizedGainLoss()));
        sb.append(CsvUtil.row("Unrealized Gain/Loss", summary.unrealizedGainLoss()));
        sb.append(CsvUtil.row("Total Gain/Loss", summary.absoluteGainLoss()));
        sb.append(CsvUtil.row("Percent Gain/Loss", summary.percentGainLoss()));
        sb.append(CsvUtil.row("XIRR %", summary.xirr() != null ? summary.xirr().xirrPercent() : null));
        sb.append(CsvUtil.row("XIRR Status", summary.xirr() != null ? summary.xirr().status() : null));
        sb.append(CsvUtil.row("Holdings Count", summary.holdingsCount()));
        sb.append(CsvUtil.row("Partial Valuation", summary.partialValuation()));
        sb.append(CsvUtil.row("Note", "This report is informational only and is not a tax statement or official brokerage statement."));
        return sb.toString();
    }

    private TransactionResponse toResponse(finadvisor.portfolio.entity.PortfolioTransaction tx) {
        return new TransactionResponse(tx.getId(), tx.getPortfolio().getId(), tx.getAssetType(), tx.getStockSymbol(),
                tx.getStockExchange(), tx.getMutualFundSchemeCode(), tx.getTransactionType(), tx.getTransactionDate(),
                tx.getQuantity(), tx.getPricePerUnit(), tx.getGrossAmount(), tx.getFees(), tx.getTaxes(), tx.getNetAmount(),
                tx.getNotes(), tx.getExternalReference(), tx.getCreatedAt(), tx.getUpdatedAt());
    }
}
