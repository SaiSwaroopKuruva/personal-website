package finadvisor.dto.risk;

import finadvisor.entity.RiskLevel;

import java.time.Instant;
import java.util.UUID;

public record RiskAssessmentResponse(
        UUID id,
        int score,
        RiskLevel riskLevel,
        RiskRecommendationResponse recommendation,
        Instant createdAt
) {
}
