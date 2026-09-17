package finadvisor.mapper;

import finadvisor.dto.risk.RiskAssessmentResponse;
import finadvisor.dto.risk.RiskRecommendationResponse;
import finadvisor.entity.RiskAssessmentResult;
import finadvisor.entity.RiskRecommendation;
import org.springframework.stereotype.Component;

@Component
public class RiskMapper {

    public RiskAssessmentResponse toResponse(RiskAssessmentResult result) {
        return new RiskAssessmentResponse(
                result.getId(),
                result.getScore(),
                result.getRiskLevel(),
                toRecommendationResponse(result.getRecommendation()),
                result.getCreatedAt());
    }

    public RiskRecommendationResponse toRecommendationResponse(RiskRecommendation recommendation) {
        return new RiskRecommendationResponse(
                recommendation.getSummary(),
                recommendation.getSuggestedAllocation(),
                recommendation.getSuggestedInvestmentHorizon(),
                recommendation.getSuggestedFundCategories());
    }
}
