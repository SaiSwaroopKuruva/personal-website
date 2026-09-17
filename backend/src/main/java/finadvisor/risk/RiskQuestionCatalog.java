package finadvisor.risk;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Defines the configurable investor risk questionnaire. Each question carries equal weight;
 * the engine averages the selected option scores (0-100) into a single risk score.
 * Extend this catalog (or externalize it to configuration/DB) to tune the questionnaire.
 */
@Component
public class RiskQuestionCatalog {

    private final List<RiskQuestionDefinition> questions = List.of(
            question("AGE", "What is your age group?", null,
                    option("UNDER_30", "Under 30", 100),
                    option("30_45", "30 - 45", 67),
                    option("46_60", "46 - 60", 33),
                    option("ABOVE_60", "Above 60", 0)),

            question("INCOME", "What is your annual income range?", null,
                    option("LOW", "Below ₹5 lakh", 0),
                    option("MODERATE", "₹5 lakh - ₹15 lakh", 33),
                    option("HIGH", "₹15 lakh - ₹40 lakh", 67),
                    option("VERY_HIGH", "Above ₹40 lakh", 100)),

            question("DEPENDENTS", "How many financial dependents do you have?", null,
                    option("NONE", "None", 100),
                    option("ONE_TWO", "1 - 2", 67),
                    option("THREE_FOUR", "3 - 4", 33),
                    option("FIVE_PLUS", "5 or more", 0)),

            question("INVESTMENT_HORIZON", "How long can you stay invested before needing the money?", null,
                    option("SHORT", "Less than 3 years", 0),
                    option("MEDIUM", "3 - 7 years", 33),
                    option("LONG", "7 - 15 years", 67),
                    option("VERY_LONG", "More than 15 years", 100)),

            question("INVESTMENT_EXPERIENCE", "How would you describe your investing experience?", null,
                    option("NONE", "No prior experience", 0),
                    option("BEGINNER", "Some experience with mutual funds", 33),
                    option("INTERMEDIATE", "Experienced with equities and mutual funds", 67),
                    option("EXPERT", "Extensive experience across asset classes", 100)),

            question("MARKET_KNOWLEDGE", "How well do you understand market fluctuations?", null,
                    option("LOW", "I don't follow markets", 0),
                    option("BASIC", "I have a basic understanding", 33),
                    option("GOOD", "I understand market cycles well", 67),
                    option("EXPERT", "I actively track and analyse markets", 100)),

            question("REACTION_TO_LOSS", "If your portfolio fell 20% in a month, what would you do?", null,
                    option("SELL_ALL", "Sell everything immediately", 0),
                    option("SELL_SOME", "Sell a portion to limit losses", 33),
                    option("HOLD", "Hold and wait for recovery", 67),
                    option("BUY_MORE", "Invest more to average down", 100)),

            question("EMERGENCY_FUND", "Do you have an emergency fund covering at least 6 months of expenses?", null,
                    option("NONE", "No emergency fund", 0),
                    option("PARTIAL", "Covers 1 - 3 months", 33),
                    option("ADEQUATE", "Covers 4 - 6 months", 67),
                    option("STRONG", "Covers more than 6 months", 100)),

            question("INVESTMENT_OBJECTIVE", "What is your primary investment objective?", null,
                    option("CAPITAL_PRESERVATION", "Preserve capital", 0),
                    option("REGULAR_INCOME", "Generate regular income", 33),
                    option("BALANCED_GROWTH", "Balanced growth with some income", 67),
                    option("WEALTH_CREATION", "Maximize long-term wealth creation", 100)),

            question("EXPECTED_RETURNS", "What annual return do you expect from your investments?", null,
                    option("LOW", "Up to 6%", 0),
                    option("MODERATE", "7% - 10%", 33),
                    option("HIGH", "11% - 15%", 67),
                    option("VERY_HIGH", "Above 15%", 100)),

            question("CURRENT_INVESTMENTS", "How is your existing portfolio allocated?", null,
                    option("ALL_DEBT", "Entirely in fixed deposits / debt", 0),
                    option("MOSTLY_DEBT", "Mostly debt with some equity", 33),
                    option("MOSTLY_EQUITY", "Mostly equity with some debt", 67),
                    option("ALL_EQUITY", "Entirely in equity / high-growth assets", 100)),

            question("DEBT_OBLIGATIONS", "How significant are your current debt obligations (EMIs/loans)?", null,
                    option("HIGH", "High - more than 50% of income", 0),
                    option("MODERATE", "Moderate - 25% - 50% of income", 33),
                    option("LOW", "Low - less than 25% of income", 67),
                    option("NONE", "No debt obligations", 100)),

            question("LIQUIDITY_NEEDS", "How soon might you need to access a large portion of this investment?", null,
                    option("IMMEDIATE", "Within a few months", 0),
                    option("SHORT_TERM", "Within 1 - 2 years", 33),
                    option("MEDIUM_TERM", "Within 3 - 5 years", 67),
                    option("NONE", "No foreseeable need", 100)),

            question("RISK_APPETITE", "Which statement best describes your risk appetite?", null,
                    option("VERY_LOW", "I want to avoid any loss of capital", 0),
                    option("LOW", "I accept small fluctuations for modest gains", 33),
                    option("HIGH", "I accept significant fluctuations for higher gains", 67),
                    option("VERY_HIGH", "I actively seek high-risk, high-reward opportunities", 100)),

            question("TAX_SAVING_PREFERENCE", "How important is tax saving in your investment decisions?", null,
                    option("VERY_IMPORTANT", "It is my primary consideration", 0),
                    option("IMPORTANT", "It is an important factor", 33),
                    option("SOMEWHAT", "It is a minor factor", 67),
                    option("NOT_IMPORTANT", "Not a consideration at all", 100))
    );

    public List<RiskQuestionDefinition> getQuestions() {
        return questions;
    }

    private static RiskQuestionDefinition question(String code, String text, String helpText, RiskOptionDefinition... options) {
        return new RiskQuestionDefinition(code, text, helpText, List.of(options));
    }

    private static RiskOptionDefinition option(String code, String label, int score) {
        return new RiskOptionDefinition(code, label, score);
    }
}
