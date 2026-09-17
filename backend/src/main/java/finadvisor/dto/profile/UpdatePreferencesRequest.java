package finadvisor.dto.profile;

import finadvisor.entity.InvestmentExperience;
import finadvisor.entity.InvestmentHorizon;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public record UpdatePreferencesRequest(
        InvestmentExperience investmentExperience,
        InvestmentHorizon investmentHorizon,

        @DecimalMin(value = "0", message = "Monthly investment budget cannot be negative")
        BigDecimal monthlyInvestmentBudget
) {
}
