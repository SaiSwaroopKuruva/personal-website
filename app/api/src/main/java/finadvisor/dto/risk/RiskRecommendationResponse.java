package finadvisor.dto.risk;

import java.util.List;
import java.util.Map;

public record RiskRecommendationResponse(
        String summary,
        Map<String, Integer> suggestedAllocation,
        String suggestedInvestmentHorizon,
        List<String> suggestedFundCategories
) {
}
