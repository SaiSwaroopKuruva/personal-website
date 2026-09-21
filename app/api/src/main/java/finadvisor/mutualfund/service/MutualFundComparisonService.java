package finadvisor.mutualfund.service;

import finadvisor.mutualfund.dto.ComparisonResponse;

import java.util.List;

public interface MutualFundComparisonService {
    ComparisonResponse compare(List<String> schemeCodes);
}
