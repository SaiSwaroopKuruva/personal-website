package finadvisor.portfolio.service;

import finadvisor.dto.PageResponse;
import finadvisor.portfolio.dto.TransactionRequest;
import finadvisor.portfolio.dto.TransactionResponse;
import finadvisor.portfolio.entity.AssetType;
import finadvisor.portfolio.entity.PortfolioTransaction;
import finadvisor.portfolio.entity.TransactionType;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PortfolioTransactionService {

    PageResponse<TransactionResponse> listTransactions(
            String userEmail, UUID portfolioId, AssetType assetType, String symbol, TransactionType transactionType,
            LocalDate dateFrom, LocalDate dateTo, int page, int size);

    TransactionResponse createTransaction(String userEmail, UUID portfolioId, TransactionRequest request);

    TransactionResponse getTransaction(String userEmail, UUID portfolioId, UUID transactionId);

    TransactionResponse updateTransaction(String userEmail, UUID portfolioId, UUID transactionId, TransactionRequest request);

    void deleteTransaction(String userEmail, UUID portfolioId, UUID transactionId);

    /** Full chronological ledger for a portfolio the caller already owns - used by analytics/export/snapshot services. */
    List<PortfolioTransaction> loadLedger(UUID portfolioId);
}
