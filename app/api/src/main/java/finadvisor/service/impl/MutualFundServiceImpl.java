package finadvisor.service.impl;

import finadvisor.config.CacheConfig;
import finadvisor.dto.PageResponse;
import finadvisor.dto.mutualfund.AmcOptionResponse;
import finadvisor.dto.mutualfund.FilterMetadataResponse;
import finadvisor.dto.mutualfund.FundManagerResponse;
import finadvisor.dto.mutualfund.HoldingResponse;
import finadvisor.dto.mutualfund.MutualFundDetailsResponse;
import finadvisor.dto.mutualfund.MutualFundSummaryResponse;
import finadvisor.dto.mutualfund.NavHistoryResponse;
import finadvisor.dto.mutualfund.NavPointResponse;
import finadvisor.dto.mutualfund.ReturnsResponse;
import finadvisor.entity.FundStatus;
import finadvisor.entity.MutualFund;
import finadvisor.entity.MutualFundHolding;
import finadvisor.entity.MutualFundNavHistory;
import finadvisor.entity.MutualFundReturn;
import finadvisor.entity.ReturnPeriod;
import finadvisor.entity.User;
import finadvisor.exception.MutualFundNotFoundException;
import finadvisor.mapper.MutualFundMapper;
import finadvisor.repository.MutualFundAmcRepository;
import finadvisor.repository.MutualFundHoldingRepository;
import finadvisor.repository.MutualFundManagerRepository;
import finadvisor.repository.MutualFundNavHistoryRepository;
import finadvisor.repository.MutualFundRepository;
import finadvisor.repository.MutualFundReturnRepository;
import finadvisor.repository.MutualFundSpecifications;
import finadvisor.repository.UserMutualFundFavoriteRepository;
import finadvisor.repository.UserRepository;
import finadvisor.service.MutualFundService;
import finadvisor.util.FinancialDisclaimers;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MutualFundServiceImpl implements MutualFundService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "schemeName", "nav", "aum", "expenseRatio", "navDate", "category");

    private final MutualFundRepository mutualFundRepository;
    private final MutualFundAmcRepository mutualFundAmcRepository;
    private final MutualFundNavHistoryRepository navHistoryRepository;
    private final MutualFundReturnRepository returnRepository;
    private final MutualFundHoldingRepository holdingRepository;
    private final MutualFundManagerRepository managerRepository;
    private final UserMutualFundFavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final MutualFundMapper mapper;

    @Override
    public PageResponse<MutualFundSummaryResponse> search(String search, String amc, String category, String subCategory,
                                                            String planType, String optionType, String riskLevel,
                                                            BigDecimal minAum, BigDecimal maxAum, BigDecimal minExpenseRatio,
                                                            BigDecimal maxExpenseRatio, String sort, String direction,
                                                            int page, int size, String currentUserEmail) {
        Sort resolvedSort = resolveSort(sort, direction);
        Pageable pageable = PageRequest.of(Math.max(page, 0), clampSize(size), resolvedSort);
        Specification<MutualFund> spec = MutualFundSpecifications.matching(search, amc, category, subCategory,
                planType, optionType, riskLevel, minAum, maxAum, minExpenseRatio, maxExpenseRatio);

        Page<MutualFund> result = mutualFundRepository.findAll(spec, pageable);
        List<UUID> fundIds = result.getContent().stream().map(MutualFund::getId).toList();
        Map<UUID, Map<ReturnPeriod, BigDecimal>> returnsByFund = loadReturnsByFund(fundIds);
        Set<UUID> favoriteFundIds = loadFavoriteFundIds(currentUserEmail, fundIds);

        Page<MutualFundSummaryResponse> mapped = result.map(fund -> mapper.toSummary(fund,
                returnsByFund.getOrDefault(fund.getId(), Map.of()), favoriteFundIds.contains(fund.getId())));
        return PageResponse.of(mapped);
    }

    @Override
    public MutualFundDetailsResponse getDetails(String schemeCode, String currentUserEmail) {
        MutualFund fund = findActiveFund(schemeCode);
        List<MutualFundReturn> returns = returnRepository.findByMutualFund_Id(fund.getId());
        List<finadvisor.entity.MutualFundManager> managers = managerRepository.findByMutualFund_IdOrderByActiveDescJoiningDateDesc(fund.getId());
        LocalDate holdingsAsOfDate = holdingRepository.findLatestAsOfDate(fund.getId()).orElse(null);
        List<MutualFundHolding> topHoldings = holdingsAsOfDate == null
                ? List.of()
                : holdingRepository.findByMutualFund_Id(fund.getId(), PageRequest.of(0, 10,
                        Sort.by(Sort.Direction.DESC, "weightPercentage"))).getContent();

        boolean favorite = isFavorite(currentUserEmail, fund.getId());
        return mapper.toDetails(fund, returns, managers, topHoldings, holdingsAsOfDate, favorite);
    }

    @Override
    public NavHistoryResponse getNavHistory(String schemeCode, LocalDate from, LocalDate to, String interval) {
        MutualFund fund = findActiveFund(schemeCode);
        LocalDate effectiveTo = to != null ? to : LocalDate.now();
        LocalDate effectiveFrom = from != null ? from : effectiveTo.minusYears(1);
        if (effectiveFrom.isAfter(effectiveTo)) {
            throw new IllegalArgumentException("'from' date must not be after 'to' date");
        }

        List<MutualFundNavHistory> history = navHistoryRepository
                .findByMutualFund_IdAndNavDateBetweenOrderByNavDateAsc(fund.getId(), effectiveFrom, effectiveTo);

        String resolvedInterval = interval == null ? "DAILY" : interval.trim().toUpperCase();
        List<NavPointResponse> points = downsample(history, resolvedInterval);
        return new NavHistoryResponse(fund.getSchemeCode(), resolvedInterval, points);
    }

    @Override
    public ReturnsResponse getReturns(String schemeCode) {
        MutualFund fund = findActiveFund(schemeCode);
        List<MutualFundReturn> returns = returnRepository.findByMutualFund_Id(fund.getId());
        return new ReturnsResponse(fund.getSchemeCode(), returns.stream().map(mapper::toReturn).toList(),
                FinancialDisclaimers.MUTUAL_FUND_GENERAL);
    }

    @Override
    public PageResponse<HoldingResponse> getHoldings(String schemeCode, String sector, String assetType, LocalDate asOfDate, int page, int size) {
        MutualFund fund = findActiveFund(schemeCode);
        LocalDate effectiveAsOfDate = asOfDate != null ? asOfDate : holdingRepository.findLatestAsOfDate(fund.getId()).orElse(null);
        if (effectiveAsOfDate == null) {
            return new PageResponse<>(List.of(), page, size, 0, 0);
        }

        Specification<MutualFundHolding> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("mutualFund").get("id"), fund.getId()));
            predicates.add(cb.equal(root.get("asOfDate"), effectiveAsOfDate));
            if (sector != null && !sector.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("sector")), sector.trim().toLowerCase()));
            }
            if (assetType != null && !assetType.isBlank()) {
                predicates.add(cb.equal(root.get("assetType"), finadvisor.entity.HoldingAssetType.valueOf(assetType.trim().toUpperCase())));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(Math.max(page, 0), clampSize(size), Sort.by(Sort.Direction.DESC, "weightPercentage"));
        Page<MutualFundHolding> result = holdingRepository.findAll(spec, pageable);
        return PageResponse.of(result.map(mapper::toHolding));
    }

    @Override
    public List<FundManagerResponse> getManagers(String schemeCode) {
        MutualFund fund = findActiveFund(schemeCode);
        return managerRepository.findByMutualFund_IdOrderByActiveDescJoiningDateDesc(fund.getId())
                .stream().map(mapper::toManager).toList();
    }

    @Override
    @Cacheable(CacheConfig.FUND_FILTERS_CACHE)
    public FilterMetadataResponse getFilterMetadata() {
        List<AmcOptionResponse> amcs = mutualFundAmcRepository.findAllByStatusOrderByNameAsc(FundStatus.ACTIVE).stream()
                .map(a -> new AmcOptionResponse(a.getCode(), a.getName())).toList();
        List<MutualFund> activeFunds = mutualFundRepository.findAll((root, query, cb) ->
                cb.notEqual(root.get("status"), FundStatus.INACTIVE));

        List<String> categories = distinctSorted(activeFunds.stream().map(MutualFund::getCategory));
        List<String> subCategories = distinctSorted(activeFunds.stream().map(MutualFund::getSubCategory));
        List<String> riskLevels = distinctSorted(activeFunds.stream().map(f -> f.getRiskLevel().name()));
        List<String> planTypes = distinctSorted(activeFunds.stream().map(f -> f.getPlanType().name()));
        List<String> optionTypes = distinctSorted(activeFunds.stream().map(f -> f.getOptionType().name()));
        List<String> assetClasses = distinctSorted(activeFunds.stream().map(f -> f.getAssetClass().name()));

        return new FilterMetadataResponse(amcs, categories, subCategories, riskLevels, planTypes, optionTypes, assetClasses);
    }

    private List<String> distinctSorted(java.util.stream.Stream<String> stream) {
        return stream.filter(v -> v != null && !v.isBlank()).distinct().sorted().toList();
    }

    private MutualFund findActiveFund(String schemeCode) {
        return mutualFundRepository.findBySchemeCodeAndStatusNot(schemeCode, FundStatus.INACTIVE)
                .orElseThrow(() -> new MutualFundNotFoundException("Mutual fund not found: " + schemeCode));
    }

    private Sort resolveSort(String sort, String direction) {
        String field = (sort == null || sort.isBlank() || !ALLOWED_SORT_FIELDS.contains(sort)) ? "schemeName" : sort;
        Sort.Direction dir = "DESC".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(dir, field);
    }

    private int clampSize(int size) {
        if (size <= 0) {
            return 20;
        }
        return Math.min(size, 100);
    }

    private Map<UUID, Map<ReturnPeriod, BigDecimal>> loadReturnsByFund(List<UUID> fundIds) {
        if (fundIds.isEmpty()) {
            return Map.of();
        }
        return returnRepository.findByMutualFund_IdIn(fundIds).stream()
                .collect(Collectors.groupingBy(r -> r.getMutualFund().getId(),
                        Collectors.toMap(MutualFundReturn::getReturnPeriod, MutualFundReturn::getReturnPercentage, (a, b) -> a)));
    }

    private Set<UUID> loadFavoriteFundIds(String currentUserEmail, List<UUID> fundIds) {
        if (currentUserEmail == null || fundIds.isEmpty()) {
            return Collections.emptySet();
        }
        return userRepository.findByEmail(currentUserEmail)
                .map(user -> new HashSet<>(favoriteRepository.findFavoritedFundIds(user.getId(), fundIds)))
                .orElseGet(HashSet::new);
    }

    private boolean isFavorite(String currentUserEmail, UUID fundId) {
        if (currentUserEmail == null) {
            return false;
        }
        return userRepository.findByEmail(currentUserEmail)
                .map(User::getId)
                .map(userId -> favoriteRepository.existsByUser_IdAndMutualFund_Id(userId, fundId))
                .orElse(false);
    }

    private List<NavPointResponse> downsample(List<MutualFundNavHistory> history, String interval) {
        if ("DAILY".equals(interval) || history.isEmpty()) {
            return history.stream().map(h -> new NavPointResponse(h.getNavDate(), h.getNav())).toList();
        }
        long stepDays = "MONTHLY".equals(interval) ? 30 : 7;
        List<NavPointResponse> points = new ArrayList<>();
        LocalDate lastIncluded = null;
        for (MutualFundNavHistory point : history) {
            if (lastIncluded == null || ChronoUnit.DAYS.between(lastIncluded, point.getNavDate()) >= stepDays) {
                points.add(new NavPointResponse(point.getNavDate(), point.getNav()));
                lastIncluded = point.getNavDate();
            }
        }
        MutualFundNavHistory last = history.get(history.size() - 1);
        if (points.isEmpty() || !points.get(points.size() - 1).date().equals(last.getNavDate())) {
            points.add(new NavPointResponse(last.getNavDate(), last.getNav()));
        }
        return points;
    }
}
