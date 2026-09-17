package finadvisor.dto.risk;

import jakarta.validation.constraints.NotBlank;

public record RiskAnswerRequest(
        @NotBlank(message = "Question code is required")
        String questionCode,

        @NotBlank(message = "Option code is required")
        String optionCode
) {
}
