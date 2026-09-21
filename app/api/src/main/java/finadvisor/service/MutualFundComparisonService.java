package finadvisor.service;

import finadvisor.dto.mutualfund.ComparisonResponse;

import java.util.List;

public interface MutualFundComparisonService {
    ComparisonResponse compare(List<String> schemeCodes);
}
