package finadvisor.portfolio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePortfolioRequest(
        @NotBlank @Size(max = 150) String name,
        @Size(max = 1000) String description,
        Boolean isDefault
) {
}
