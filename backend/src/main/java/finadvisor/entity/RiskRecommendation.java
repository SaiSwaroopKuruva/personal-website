package finadvisor.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/** Stored as JSONB on {@code risk_assessment_results.recommendation}. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskRecommendation {
    private String summary;
    private Map<String, Integer> suggestedAllocation;
    private String suggestedInvestmentHorizon;
    private List<String> suggestedFundCategories;
}
