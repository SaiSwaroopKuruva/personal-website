package finadvisor.risk;

import finadvisor.dto.risk.RiskAnswerRequest;
import finadvisor.entity.RiskLevel;
import finadvisor.entity.RiskRecommendation;
import finadvisor.exception.InvalidRiskAnswerException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Scores a submitted questionnaire and derives the investor risk recommendation. */
@Component
@RequiredArgsConstructor
public class RiskEngine {

    private final RiskQuestionCatalog catalog;

    public int calculateScore(List<RiskAnswerRequest> answers) {
        Map<String, RiskQuestionDefinition> questionsByCode = new LinkedHashMap<>();
        catalog.getQuestions().forEach(q -> questionsByCode.put(q.code(), q));

        double total = 0;
        for (RiskAnswerRequest answer : answers) {
            RiskQuestionDefinition question = questionsByCode.get(answer.questionCode());
            if (question == null) {
                throw new InvalidRiskAnswerException("Unknown question code: " + answer.questionCode());
            }
            RiskOptionDefinition option = question.option(answer.optionCode());
            if (option == null) {
                throw new InvalidRiskAnswerException("Unknown option code '" + answer.optionCode()
                        + "' for question " + answer.questionCode());
            }
            total += option.score();
        }
        return (int) Math.round(total / answers.size());
    }

    public RiskLevel resolveRiskLevel(int score) {
        if (score <= 25) {
            return RiskLevel.CONSERVATIVE;
        } else if (score <= 50) {
            return RiskLevel.MODERATELY_CONSERVATIVE;
        } else if (score <= 70) {
            return RiskLevel.BALANCED;
        } else if (score <= 85) {
            return RiskLevel.GROWTH;
        }
        return RiskLevel.AGGRESSIVE;
    }

    public RiskRecommendation buildRecommendation(RiskLevel riskLevel) {
        return switch (riskLevel) {
            case CONSERVATIVE -> RiskRecommendation.builder()
                    .summary("You prioritize capital safety. A debt-heavy allocation with minimal equity exposure suits your profile.")
                    .suggestedAllocation(allocation(60, 5, 0, 0, 5, 10, 20))
                    .suggestedInvestmentHorizon("Short to medium term (1 - 3 years)")
                    .suggestedFundCategories(List.of("Liquid Funds", "Short Duration Debt Funds", "Banking & PSU Funds"))
                    .build();
            case MODERATELY_CONSERVATIVE -> RiskRecommendation.builder()
                    .summary("You prefer stability with limited growth exposure. A conservative hybrid allocation balances safety and returns.")
                    .suggestedAllocation(allocation(45, 15, 5, 0, 10, 10, 15))
                    .suggestedInvestmentHorizon("Medium term (3 - 5 years)")
                    .suggestedFundCategories(List.of("Conservative Hybrid Funds", "Short Duration Debt Funds", "Large Cap Funds"))
                    .build();
            case BALANCED -> RiskRecommendation.builder()
                    .summary("You are comfortable balancing growth and stability. An equal mix of equity and debt suits your goals.")
                    .suggestedAllocation(allocation(30, 25, 10, 5, 10, 10, 10))
                    .suggestedInvestmentHorizon("Medium to long term (5 - 7 years)")
                    .suggestedFundCategories(List.of("Balanced Advantage Funds", "Large & Mid Cap Funds", "Corporate Bond Funds"))
                    .build();
            case GROWTH -> RiskRecommendation.builder()
                    .summary("You seek long-term growth and can tolerate market volatility. An equity-heavy allocation fits your objectives.")
                    .suggestedAllocation(allocation(15, 30, 20, 10, 10, 10, 5))
                    .suggestedInvestmentHorizon("Long term (7 - 10 years)")
                    .suggestedFundCategories(List.of("Flexi Cap Funds", "Mid Cap Funds", "International Funds"))
                    .build();
            case AGGRESSIVE -> RiskRecommendation.builder()
                    .summary("You are focused on maximizing long-term wealth and are comfortable with high volatility.")
                    .suggestedAllocation(allocation(5, 30, 25, 20, 10, 5, 5))
                    .suggestedInvestmentHorizon("Long term (10+ years)")
                    .suggestedFundCategories(List.of("Small Cap Funds", "Mid Cap Funds", "Sectoral & Thematic Funds", "International Funds"))
                    .build();
        };
    }

    private Map<String, Integer> allocation(int debt, int largeCap, int midCap, int smallCap, int gold, int international, int cash) {
        Map<String, Integer> allocation = new LinkedHashMap<>();
        allocation.put("Debt", debt);
        allocation.put("Large Cap", largeCap);
        allocation.put("Mid Cap", midCap);
        allocation.put("Small Cap", smallCap);
        allocation.put("Gold", gold);
        allocation.put("International", international);
        allocation.put("Cash", cash);
        return allocation;
    }
}
