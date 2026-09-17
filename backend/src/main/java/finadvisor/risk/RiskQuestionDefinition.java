package finadvisor.risk;

import java.util.List;

/** A single configurable risk-questionnaire question with its scorable options. */
public record RiskQuestionDefinition(String code, String text, String helpText, List<RiskOptionDefinition> options) {

    public RiskOptionDefinition option(String optionCode) {
        return options.stream()
                .filter(option -> option.code().equals(optionCode))
                .findFirst()
                .orElse(null);
    }
}
