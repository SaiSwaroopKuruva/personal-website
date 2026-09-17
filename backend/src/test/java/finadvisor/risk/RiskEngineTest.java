package finadvisor.risk;

import finadvisor.dto.risk.RiskAnswerRequest;
import finadvisor.entity.RiskLevel;
import finadvisor.entity.RiskRecommendation;
import finadvisor.exception.InvalidRiskAnswerException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RiskEngineTest {

    private final RiskEngine riskEngine = new RiskEngine(new RiskQuestionCatalog());

    @Test
    void calculateScore_shouldReturnZeroForAllLowestScoringOptions() {
        List<RiskAnswerRequest> answers = List.of(
                new RiskAnswerRequest("AGE", "ABOVE_60"),
                new RiskAnswerRequest("INCOME", "LOW"));

        int score = riskEngine.calculateScore(answers);

        assertThat(score).isZero();
    }

    @Test
    void calculateScore_shouldReturnHundredForAllHighestScoringOptions() {
        List<RiskAnswerRequest> answers = List.of(
                new RiskAnswerRequest("AGE", "UNDER_30"),
                new RiskAnswerRequest("RISK_APPETITE", "VERY_HIGH"));

        int score = riskEngine.calculateScore(answers);

        assertThat(score).isEqualTo(100);
    }

    @Test
    void calculateScore_shouldThrowForUnknownQuestionCode() {
        List<RiskAnswerRequest> answers = List.of(new RiskAnswerRequest("UNKNOWN", "X"));

        assertThatThrownBy(() -> riskEngine.calculateScore(answers))
                .isInstanceOf(InvalidRiskAnswerException.class);
    }

    @Test
    void calculateScore_shouldThrowForUnknownOptionCode() {
        List<RiskAnswerRequest> answers = List.of(new RiskAnswerRequest("AGE", "UNKNOWN_OPTION"));

        assertThatThrownBy(() -> riskEngine.calculateScore(answers))
                .isInstanceOf(InvalidRiskAnswerException.class);
    }

    @Test
    void resolveRiskLevel_shouldMapScoreBandsCorrectly() {
        assertThat(riskEngine.resolveRiskLevel(0)).isEqualTo(RiskLevel.CONSERVATIVE);
        assertThat(riskEngine.resolveRiskLevel(25)).isEqualTo(RiskLevel.CONSERVATIVE);
        assertThat(riskEngine.resolveRiskLevel(26)).isEqualTo(RiskLevel.MODERATELY_CONSERVATIVE);
        assertThat(riskEngine.resolveRiskLevel(50)).isEqualTo(RiskLevel.MODERATELY_CONSERVATIVE);
        assertThat(riskEngine.resolveRiskLevel(51)).isEqualTo(RiskLevel.BALANCED);
        assertThat(riskEngine.resolveRiskLevel(70)).isEqualTo(RiskLevel.BALANCED);
        assertThat(riskEngine.resolveRiskLevel(71)).isEqualTo(RiskLevel.GROWTH);
        assertThat(riskEngine.resolveRiskLevel(85)).isEqualTo(RiskLevel.GROWTH);
        assertThat(riskEngine.resolveRiskLevel(86)).isEqualTo(RiskLevel.AGGRESSIVE);
        assertThat(riskEngine.resolveRiskLevel(100)).isEqualTo(RiskLevel.AGGRESSIVE);
    }

    @Test
    void buildRecommendation_shouldProduceAllocationSummingToHundred() {
        for (RiskLevel level : RiskLevel.values()) {
            RiskRecommendation recommendation = riskEngine.buildRecommendation(level);
            int total = recommendation.getSuggestedAllocation().values().stream().mapToInt(Integer::intValue).sum();
            assertThat(total).as("allocation for %s", level).isEqualTo(100);
            assertThat(recommendation.getSummary()).isNotBlank();
            assertThat(recommendation.getSuggestedFundCategories()).isNotEmpty();
        }
    }
}
