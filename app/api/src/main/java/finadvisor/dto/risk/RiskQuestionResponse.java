package finadvisor.dto.risk;

import java.util.List;

public record RiskQuestionResponse(
        String code,
        String text,
        String helpText,
        List<RiskOptionResponse> options
) {
}
