package finadvisor.networth.dto;

import finadvisor.networth.entity.LiabilityCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LiabilityRequest(
        @NotBlank @Size(max = 150) String name,
        @NotNull LiabilityCategory category,
        @NotNull @DecimalMin(value = "0", inclusive = true) BigDecimal currentValue,
        @Size(min = 3, max = 3) String currency,
        @NotNull LocalDate valuationDate,
        @Size(max = 1000) String notes
) {
}
