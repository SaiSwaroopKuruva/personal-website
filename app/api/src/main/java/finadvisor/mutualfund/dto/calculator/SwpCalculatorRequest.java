package finadvisor.mutualfund.dto.calculator;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record SwpCalculatorRequest(
        @NotNull @Positive BigDecimal initialInvestment,
        @NotNull @PositiveOrZero BigDecimal withdrawalPerMonth,
        @NotNull @DecimalMin(value = "0.0") @DecimalMax(value = "100.0") BigDecimal expectedAnnualReturn,
        @NotNull @Positive Integer durationYears
) {
}
