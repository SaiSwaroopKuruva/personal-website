package finadvisor.mutualfund.dto.calculator;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record SipCalculatorRequest(
        @NotNull @Positive BigDecimal monthlyInvestment,
        @NotNull @DecimalMin(value = "0.0") @DecimalMax(value = "100.0") BigDecimal expectedAnnualReturn,
        @NotNull @Positive Integer durationYears
) {
}
