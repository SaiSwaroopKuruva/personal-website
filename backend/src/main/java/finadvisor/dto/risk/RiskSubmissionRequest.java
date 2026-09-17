package finadvisor.dto.risk;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record RiskSubmissionRequest(
        @NotEmpty(message = "At least one answer is required")
        @Valid
        List<RiskAnswerRequest> answers
) {
}
