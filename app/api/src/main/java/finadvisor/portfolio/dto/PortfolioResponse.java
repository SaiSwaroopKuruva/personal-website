package finadvisor.portfolio.dto;

import java.time.Instant;
import java.util.UUID;

public record PortfolioResponse(
        UUID id,
        String name,
        String description,
        String baseCurrency,
        boolean isDefault,
        Instant createdAt,
        Instant updatedAt,
        Instant archivedAt
) {
}
