package finadvisor.mutualfund.service.impl;

import finadvisor.mutualfund.dto.ComparisonResponse;
import finadvisor.mutualfund.entity.FundStatus;
import finadvisor.mutualfund.entity.MutualFund;
import finadvisor.mutualfund.entity.MutualFundReturn;
import finadvisor.mutualfund.entity.ReturnPeriod;
import finadvisor.mutualfund.exception.InvalidComparisonRequestException;
import finadvisor.mutualfund.mapper.MutualFundMapper;
import finadvisor.mutualfund.repository.MutualFundRepository;
import finadvisor.mutualfund.repository.MutualFundReturnRepository;
import finadvisor.mutualfund.service.MutualFundComparisonService;
import finadvisor.mutualfund.util.FinancialDisclaimers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MutualFundComparisonServiceImpl implements MutualFundComparisonService {

    private static final int MAX_FUNDS_TO_COMPARE = 4;

    private final MutualFundRepository mutualFundRepository;
    private final MutualFundReturnRepository returnRepository;
    private final MutualFundMapper mapper;

    @Override
    public ComparisonResponse compare(List<String> schemeCodes) {
        List<String> distinctCodes = schemeCodes.stream().distinct().toList();
        if (distinctCodes.size() < 2) {
            throw new InvalidComparisonRequestException("Provide at least 2 distinct scheme codes to compare");
        }
        if (distinctCodes.size() > MAX_FUNDS_TO_COMPARE) {
            throw new InvalidComparisonRequestException("You can compare at most " + MAX_FUNDS_TO_COMPARE + " mutual funds at a time");
        }

        List<MutualFund> funds = mutualFundRepository.findBySchemeCodeInAndStatusNot(distinctCodes, FundStatus.INACTIVE);
        if (funds.size() != distinctCodes.size()) {
            throw new InvalidComparisonRequestException("One or more scheme codes could not be found");
        }

        // Preserve the caller-requested order rather than the DB's natural order
        Map<String, MutualFund> byCode = funds.stream().collect(Collectors.toMap(MutualFund::getSchemeCode, f -> f, (a, b) -> a, LinkedHashMap::new));

        List<UUID> fundIds = funds.stream().map(MutualFund::getId).toList();
        Map<UUID, Map<ReturnPeriod, BigDecimal>> returnsByFund = returnRepository.findByMutualFund_IdIn(fundIds).stream()
                .collect(Collectors.groupingBy(r -> r.getMutualFund().getId(),
                        Collectors.toMap(MutualFundReturn::getReturnPeriod, MutualFundReturn::getReturnPercentage, (a, b) -> a)));

        var comparisons = distinctCodes.stream()
                .map(byCode::get)
                .map(fund -> mapper.toComparison(fund, returnsByFund.getOrDefault(fund.getId(), Map.of())))
                .toList();

        return new ComparisonResponse(comparisons, FinancialDisclaimers.COMPARISON);
    }
}
