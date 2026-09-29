package finadvisor.portfolio.service.impl;

import finadvisor.dto.PageResponse;
import finadvisor.portfolio.dto.TransactionRequest;
import finadvisor.portfolio.dto.TransactionResponse;
import finadvisor.portfolio.entity.AssetType;
import finadvisor.portfolio.entity.Portfolio;
import finadvisor.portfolio.entity.PortfolioTransaction;
import finadvisor.portfolio.entity.TransactionType;
import finadvisor.portfolio.exception.InvalidPortfolioTransactionException;
import finadvisor.portfolio.exception.PortfolioTransactionNotFoundException;
import finadvisor.portfolio.repository.PortfolioTransactionRepository;
import finadvisor.portfolio.service.HoldingCalculationService;
import finadvisor.portfolio.service.PortfolioService;
import finadvisor.portfolio.service.PortfolioSnapshotService;
import finadvisor.portfolio.service.PortfolioTransactionService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class PortfolioTransactionServiceImpl implements PortfolioTransactionService {

    private static final Set<TransactionType> STOCK_TYPES =
            Set.of(TransactionType.BUY, TransactionType.SELL, TransactionType.DIVIDEND, TransactionType.FEE, TransactionType.TAX, TransactionType.ADJUSTMENT);
    private static final Set<TransactionType> MUTUAL_FUND_TYPES =
            Set.of(TransactionType.PURCHASE, TransactionType.REDEMPTION, TransactionType.DIVIDEND, TransactionType.FEE, TransactionType.ADJUSTMENT);
    private static final Set<TransactionType> CASH_OUTFLOW_TYPES = Set.of(TransactionType.BUY, TransactionType.PURCHASE);
    private static final Set<TransactionType> CASH_INFLOW_TYPES = Set.of(TransactionType.SELL, TransactionType.REDEMPTION, TransactionType.DIVIDEND);

    private final PortfolioTransactionRepository transactionRepository;
    private final PortfolioService portfolioService;
    private final HoldingCalculationService holdingCalculationService;
    private final PortfolioSnapshotService snapshotService;

    public PortfolioTransactionServiceImpl(PortfolioTransactionRepository transactionRepository, PortfolioService portfolioService,
                                            HoldingCalculationService holdingCalculationService, PortfolioSnapshotService snapshotService) {
        this.transactionRepository = transactionRepository;
        this.portfolioService = portfolioService;
        this.holdingCalculationService = holdingCalculationService;
        this.snapshotService = snapshotService;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TransactionResponse> listTransactions(String userEmail, UUID portfolioId, AssetType assetType, String symbol,
                                                                TransactionType transactionType, LocalDate dateFrom, LocalDate dateTo,
                                                                int page, int size) {
        Portfolio portfolio = portfolioService.requireOwnedPortfolio(userEmail, portfolioId);
        Specification<PortfolioTransaction> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("portfolio").get("id"), portfolio.getId()));
            if (assetType != null) {
                predicates.add(cb.equal(root.get("assetType"), assetType));
            }
            if (symbol != null && !symbol.isBlank()) {
                predicates.add(cb.or(
                        cb.equal(cb.upper(root.get("stockSymbol")), symbol.toUpperCase()),
                        cb.equal(cb.upper(root.get("mutualFundSchemeCode")), symbol.toUpperCase())));
            }
            if (transactionType != null) {
                predicates.add(cb.equal(root.get("transactionType"), transactionType));
            }
            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("transactionDate"), dateFrom));
            }
            if (dateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("transactionDate"), dateTo));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "transactionDate").and(Sort.by(Sort.Direction.DESC, "createdAt")));
        var result = transactionRepository.findAll(spec, pageable).map(this::toResponse);
        return PageResponse.of(result);
    }

    @Override
    public TransactionResponse createTransaction(String userEmail, UUID portfolioId, TransactionRequest request) {
        Portfolio portfolio = portfolioService.requireOwnedPortfolio(userEmail, portfolioId);
        PortfolioTransaction candidate = buildTransaction(portfolio, request);
        List<PortfolioTransaction> existingLedger = transactionRepository.findByPortfolioIdOrderByTransactionDateAscCreatedAtAsc(portfolioId);
        holdingCalculationService.validateNewTransaction(candidate, existingLedger);
        PortfolioTransaction saved = transactionRepository.save(candidate);
        snapshotService.invalidateFrom(portfolioId, saved.getTransactionDate());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransaction(String userEmail, UUID portfolioId, UUID transactionId) {
        Portfolio portfolio = portfolioService.requireOwnedPortfolio(userEmail, portfolioId);
        return toResponse(requireTransaction(portfolio.getId(), transactionId));
    }

    @Override
    public TransactionResponse updateTransaction(String userEmail, UUID portfolioId, UUID transactionId, TransactionRequest request) {
        Portfolio portfolio = portfolioService.requireOwnedPortfolio(userEmail, portfolioId);
        PortfolioTransaction existing = requireTransaction(portfolio.getId(), transactionId);
        LocalDate earliestAffectedDate = existing.getTransactionDate().isBefore(request.transactionDate())
                ? existing.getTransactionDate() : request.transactionDate();

        PortfolioTransaction candidate = buildTransaction(portfolio, request);
        candidate.setId(existing.getId());
        List<PortfolioTransaction> ledgerWithoutThisOne = transactionRepository.findByPortfolioIdOrderByTransactionDateAscCreatedAtAsc(portfolioId)
                .stream().filter(tx -> !tx.getId().equals(transactionId)).toList();
        holdingCalculationService.validateNewTransaction(candidate, ledgerWithoutThisOne);

        existing.setAssetType(candidate.getAssetType());
        existing.setStockSymbol(candidate.getStockSymbol());
        existing.setStockExchange(candidate.getStockExchange());
        existing.setMutualFundSchemeCode(candidate.getMutualFundSchemeCode());
        existing.setTransactionType(candidate.getTransactionType());
        existing.setTransactionDate(candidate.getTransactionDate());
        existing.setQuantity(candidate.getQuantity());
        existing.setPricePerUnit(candidate.getPricePerUnit());
        existing.setGrossAmount(candidate.getGrossAmount());
        existing.setFees(candidate.getFees());
        existing.setTaxes(candidate.getTaxes());
        existing.setNetAmount(candidate.getNetAmount());
        existing.setNotes(candidate.getNotes());
        existing.setExternalReference(candidate.getExternalReference());

        PortfolioTransaction saved = transactionRepository.save(existing);
        snapshotService.invalidateFrom(portfolioId, earliestAffectedDate);
        return toResponse(saved);
    }

    @Override
    public void deleteTransaction(String userEmail, UUID portfolioId, UUID transactionId) {
        Portfolio portfolio = portfolioService.requireOwnedPortfolio(userEmail, portfolioId);
        PortfolioTransaction existing = requireTransaction(portfolio.getId(), transactionId);
        transactionRepository.delete(existing);
        snapshotService.invalidateFrom(portfolioId, existing.getTransactionDate());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PortfolioTransaction> loadLedger(UUID portfolioId) {
        return transactionRepository.findByPortfolioIdOrderByTransactionDateAscCreatedAtAsc(portfolioId);
    }

    private PortfolioTransaction requireTransaction(UUID portfolioId, UUID transactionId) {
        return transactionRepository.findByIdAndPortfolioId(transactionId, portfolioId)
                .orElseThrow(() -> new PortfolioTransactionNotFoundException("Transaction not found"));
    }

    private PortfolioTransaction buildTransaction(Portfolio portfolio, TransactionRequest request) {
        validate(request);
        BigDecimal fees = request.fees() != null ? request.fees() : BigDecimal.ZERO;
        BigDecimal taxes = request.taxes() != null ? request.taxes() : BigDecimal.ZERO;
        BigDecimal netAmount = computeNetAmount(request.transactionType(), request.grossAmount(), fees, taxes);

        return PortfolioTransaction.builder()
                .portfolio(portfolio)
                .assetType(request.assetType())
                .stockSymbol(request.assetType() == AssetType.STOCK ? request.stockSymbol().toUpperCase() : null)
                .stockExchange(request.assetType() == AssetType.STOCK ? request.stockExchange() : null)
                .mutualFundSchemeCode(request.assetType() == AssetType.MUTUAL_FUND ? request.mutualFundSchemeCode() : null)
                .transactionType(request.transactionType())
                .transactionDate(request.transactionDate())
                .quantity(request.quantity())
                .pricePerUnit(request.pricePerUnit())
                .grossAmount(request.grossAmount())
                .fees(fees)
                .taxes(taxes)
                .netAmount(netAmount)
                .notes(request.notes())
                .externalReference(request.externalReference())
                .build();
    }

    private void validate(TransactionRequest request) {
        if (request.assetType() == AssetType.STOCK) {
            if (request.stockSymbol() == null || request.stockSymbol().isBlank()) {
                throw new InvalidPortfolioTransactionException("stockSymbol is required for STOCK transactions");
            }
            if (!STOCK_TYPES.contains(request.transactionType())) {
                throw new InvalidPortfolioTransactionException(request.transactionType() + " is not a valid transaction type for STOCK");
            }
        } else {
            if (request.mutualFundSchemeCode() == null || request.mutualFundSchemeCode().isBlank()) {
                throw new InvalidPortfolioTransactionException("mutualFundSchemeCode is required for MUTUAL_FUND transactions");
            }
            if (!MUTUAL_FUND_TYPES.contains(request.transactionType())) {
                throw new InvalidPortfolioTransactionException(request.transactionType() + " is not a valid transaction type for MUTUAL_FUND");
            }
        }
        if (request.transactionType() != TransactionType.ADJUSTMENT && request.quantity().signum() < 0) {
            throw new InvalidPortfolioTransactionException("Only ADJUSTMENT transactions may carry a negative quantity");
        }
        if (request.transactionDate().isAfter(LocalDate.now())) {
            throw new InvalidPortfolioTransactionException("transactionDate cannot be in the future");
        }
    }

    private BigDecimal computeNetAmount(TransactionType type, BigDecimal grossAmount, BigDecimal fees, BigDecimal taxes) {
        if (type == TransactionType.ADJUSTMENT) {
            return BigDecimal.ZERO;
        }
        if (CASH_OUTFLOW_TYPES.contains(type)) {
            return grossAmount.add(fees).add(taxes);
        }
        if (CASH_INFLOW_TYPES.contains(type)) {
            return grossAmount.subtract(fees).subtract(taxes);
        }
        // FEE / TAX: the charge itself is the cash outflow.
        return grossAmount;
    }

    private TransactionResponse toResponse(PortfolioTransaction tx) {
        return new TransactionResponse(
                tx.getId(), tx.getPortfolio().getId(), tx.getAssetType(), tx.getStockSymbol(), tx.getStockExchange(),
                tx.getMutualFundSchemeCode(), tx.getTransactionType(), tx.getTransactionDate(), tx.getQuantity(),
                tx.getPricePerUnit(), tx.getGrossAmount(), tx.getFees(), tx.getTaxes(), tx.getNetAmount(),
                tx.getNotes(), tx.getExternalReference(), tx.getCreatedAt(), tx.getUpdatedAt());
    }
}
