package finadvisor.networth.service.impl;

import finadvisor.entity.User;
import finadvisor.exception.UserNotFoundException;
import finadvisor.networth.dto.AssetRequest;
import finadvisor.networth.dto.AssetResponse;
import finadvisor.networth.dto.LiabilityRequest;
import finadvisor.networth.dto.LiabilityResponse;
import finadvisor.networth.dto.NetWorthHistoryPoint;
import finadvisor.networth.dto.NetWorthHistoryResponse;
import finadvisor.networth.dto.NetWorthSummaryResponse;
import finadvisor.networth.entity.UserAsset;
import finadvisor.networth.entity.UserLiability;
import finadvisor.networth.exception.UserAssetNotFoundException;
import finadvisor.networth.exception.UserLiabilityNotFoundException;
import finadvisor.networth.repository.UserAssetRepository;
import finadvisor.networth.repository.UserLiabilityRepository;
import finadvisor.networth.service.NetWorthService;
import finadvisor.portfolio.entity.Portfolio;
import finadvisor.portfolio.entity.PortfolioTransaction;
import finadvisor.portfolio.entity.PortfolioValuationSnapshot;
import finadvisor.portfolio.repository.PortfolioRepository;
import finadvisor.portfolio.repository.PortfolioTransactionRepository;
import finadvisor.portfolio.repository.PortfolioValuationSnapshotRepository;
import finadvisor.portfolio.service.HoldingCalculationService;
import finadvisor.portfolio.service.PortfolioValuationService;
import finadvisor.portfolio.service.ValuedHolding;
import finadvisor.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Net worth = total assets - total liabilities (Part 15). Investment assets come from live portfolio
 * valuations (Part 10, may be partial); everything else comes from manually entered, dated values. This
 * app does not track cash accounts/every real-world asset, so {@code disclosure} always makes clear this
 * is a tracked, not complete, personal balance sheet.
 */
@Service
@Transactional
public class NetWorthServiceImpl implements NetWorthService {

    private static final String DISCLOSURE =
            "Net worth reflects only the investments, assets and liabilities you have recorded in FinAdvisor - "
                    + "it is not a complete, automatically-verified personal balance sheet.";

    private final UserRepository userRepository;
    private final UserAssetRepository assetRepository;
    private final UserLiabilityRepository liabilityRepository;
    private final PortfolioRepository portfolioRepository;
    private final PortfolioTransactionRepository transactionRepository;
    private final PortfolioValuationSnapshotRepository snapshotRepository;
    private final HoldingCalculationService holdingCalculationService;
    private final PortfolioValuationService valuationService;

    public NetWorthServiceImpl(UserRepository userRepository, UserAssetRepository assetRepository,
                                UserLiabilityRepository liabilityRepository, PortfolioRepository portfolioRepository,
                                PortfolioTransactionRepository transactionRepository, PortfolioValuationSnapshotRepository snapshotRepository,
                                HoldingCalculationService holdingCalculationService, PortfolioValuationService valuationService) {
        this.userRepository = userRepository;
        this.assetRepository = assetRepository;
        this.liabilityRepository = liabilityRepository;
        this.portfolioRepository = portfolioRepository;
        this.transactionRepository = transactionRepository;
        this.snapshotRepository = snapshotRepository;
        this.holdingCalculationService = holdingCalculationService;
        this.valuationService = valuationService;
    }

    @Override
    @Transactional(readOnly = true)
    public NetWorthSummaryResponse getSummary(String userEmail) {
        User user = findUser(userEmail);
        List<Portfolio> portfolios = portfolioRepository.findByUserIdAndArchivedAtIsNullOrderByCreatedAtAsc(user.getId());

        BigDecimal investmentAssets = BigDecimal.ZERO;
        boolean partial = false;
        for (Portfolio portfolio : portfolios) {
            List<PortfolioTransaction> ledger = transactionRepository.findByPortfolioIdOrderByTransactionDateAscCreatedAtAsc(portfolio.getId());
            List<ValuedHolding> valued = valuationService.valueHoldings(holdingCalculationService.calculatePositions(ledger));
            investmentAssets = investmentAssets.add(valued.stream().filter(v -> v.marketValue() != null)
                    .map(ValuedHolding::marketValue).reduce(BigDecimal.ZERO, BigDecimal::add));
            partial = partial || valued.stream().anyMatch(v -> v.marketValue() == null);
        }

        BigDecimal nonInvestmentAssets = assetRepository.findByUserIdOrderByCreatedAtAsc(user.getId()).stream()
                .map(UserAsset::getCurrentValue).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalLiabilities = liabilityRepository.findByUserIdOrderByCreatedAtAsc(user.getId()).stream()
                .map(UserLiability::getCurrentValue).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalAssets = investmentAssets.add(nonInvestmentAssets);
        BigDecimal netWorth = totalAssets.subtract(totalLiabilities);

        return new NetWorthSummaryResponse(
                totalAssets.setScale(2, RoundingMode.HALF_UP), totalLiabilities.setScale(2, RoundingMode.HALF_UP),
                netWorth.setScale(2, RoundingMode.HALF_UP), investmentAssets.setScale(2, RoundingMode.HALF_UP),
                nonInvestmentAssets.setScale(2, RoundingMode.HALF_UP), partial, Instant.now(), DISCLOSURE);
    }

    @Override
    @Transactional(readOnly = true)
    public NetWorthHistoryResponse getHistory(String userEmail) {
        User user = findUser(userEmail);
        List<Portfolio> portfolios = portfolioRepository.findByUserIdAndArchivedAtIsNullOrderByCreatedAtAsc(user.getId());

        Map<LocalDate, BigDecimal> marketValueByDate = new TreeMap<>();
        for (Portfolio portfolio : portfolios) {
            for (PortfolioValuationSnapshot snapshot : snapshotRepository.findByPortfolioIdOrderBySnapshotDateAsc(portfolio.getId())) {
                marketValueByDate.merge(snapshot.getSnapshotDate(), snapshot.getMarketValue(), BigDecimal::add);
            }
        }

        BigDecimal nonInvestmentAssets = assetRepository.findByUserIdOrderByCreatedAtAsc(user.getId()).stream()
                .map(UserAsset::getCurrentValue).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalLiabilities = liabilityRepository.findByUserIdOrderByCreatedAtAsc(user.getId()).stream()
                .map(UserLiability::getCurrentValue).reduce(BigDecimal.ZERO, BigDecimal::add);

        if (marketValueByDate.isEmpty()) {
            return new NetWorthHistoryResponse(List.of(),
                    "No historical net-worth trend is available yet - it builds up as daily portfolio valuation snapshots accumulate.");
        }

        List<NetWorthHistoryPoint> points = marketValueByDate.entrySet().stream()
                .map(e -> new NetWorthHistoryPoint(e.getKey(), e.getValue().setScale(2, RoundingMode.HALF_UP),
                        nonInvestmentAssets.setScale(2, RoundingMode.HALF_UP), totalLiabilities.setScale(2, RoundingMode.HALF_UP),
                        e.getValue().add(nonInvestmentAssets).subtract(totalLiabilities).setScale(2, RoundingMode.HALF_UP)))
                .toList();

        return new NetWorthHistoryResponse(points,
                "Manual assets and liabilities are not snapshotted historically; every point uses their current recorded values, so only the investment portion of this trend genuinely varies by date.");
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetResponse> listAssets(String userEmail) {
        return assetRepository.findByUserIdOrderByCreatedAtAsc(findUser(userEmail).getId()).stream().map(this::toResponse).toList();
    }

    @Override
    public AssetResponse createAsset(String userEmail, AssetRequest request) {
        User user = findUser(userEmail);
        UserAsset asset = UserAsset.builder()
                .user(user).name(request.name()).category(request.category()).currentValue(request.currentValue())
                .currency(request.currency() != null ? request.currency() : "INR").valuationDate(request.valuationDate())
                .notes(request.notes()).build();
        return toResponse(assetRepository.save(asset));
    }

    @Override
    public AssetResponse updateAsset(String userEmail, UUID assetId, AssetRequest request) {
        User user = findUser(userEmail);
        UserAsset asset = assetRepository.findByIdAndUserId(assetId, user.getId())
                .orElseThrow(() -> new UserAssetNotFoundException("Asset not found"));
        asset.setName(request.name());
        asset.setCategory(request.category());
        asset.setCurrentValue(request.currentValue());
        asset.setCurrency(request.currency() != null ? request.currency() : asset.getCurrency());
        asset.setValuationDate(request.valuationDate());
        asset.setNotes(request.notes());
        return toResponse(assetRepository.save(asset));
    }

    @Override
    public void deleteAsset(String userEmail, UUID assetId) {
        User user = findUser(userEmail);
        UserAsset asset = assetRepository.findByIdAndUserId(assetId, user.getId())
                .orElseThrow(() -> new UserAssetNotFoundException("Asset not found"));
        assetRepository.delete(asset);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LiabilityResponse> listLiabilities(String userEmail) {
        return liabilityRepository.findByUserIdOrderByCreatedAtAsc(findUser(userEmail).getId()).stream().map(this::toResponse).toList();
    }

    @Override
    public LiabilityResponse createLiability(String userEmail, LiabilityRequest request) {
        User user = findUser(userEmail);
        UserLiability liability = UserLiability.builder()
                .user(user).name(request.name()).category(request.category()).currentValue(request.currentValue())
                .currency(request.currency() != null ? request.currency() : "INR").valuationDate(request.valuationDate())
                .notes(request.notes()).build();
        return toResponse(liabilityRepository.save(liability));
    }

    @Override
    public LiabilityResponse updateLiability(String userEmail, UUID liabilityId, LiabilityRequest request) {
        User user = findUser(userEmail);
        UserLiability liability = liabilityRepository.findByIdAndUserId(liabilityId, user.getId())
                .orElseThrow(() -> new UserLiabilityNotFoundException("Liability not found"));
        liability.setName(request.name());
        liability.setCategory(request.category());
        liability.setCurrentValue(request.currentValue());
        liability.setCurrency(request.currency() != null ? request.currency() : liability.getCurrency());
        liability.setValuationDate(request.valuationDate());
        liability.setNotes(request.notes());
        return toResponse(liabilityRepository.save(liability));
    }

    @Override
    public void deleteLiability(String userEmail, UUID liabilityId) {
        User user = findUser(userEmail);
        UserLiability liability = liabilityRepository.findByIdAndUserId(liabilityId, user.getId())
                .orElseThrow(() -> new UserLiabilityNotFoundException("Liability not found"));
        liabilityRepository.delete(liability);
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private AssetResponse toResponse(UserAsset asset) {
        return new AssetResponse(asset.getId(), asset.getName(), asset.getCategory(), asset.getCurrentValue(),
                asset.getCurrency(), asset.getValuationDate(), asset.getNotes(), asset.getCreatedAt(), asset.getUpdatedAt());
    }

    private LiabilityResponse toResponse(UserLiability liability) {
        return new LiabilityResponse(liability.getId(), liability.getName(), liability.getCategory(), liability.getCurrentValue(),
                liability.getCurrency(), liability.getValuationDate(), liability.getNotes(), liability.getCreatedAt(), liability.getUpdatedAt());
    }
}
