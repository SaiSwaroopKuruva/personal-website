package finadvisor.networth.dto;

import finadvisor.networth.entity.AssetCategory;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AssetResponse(
        UUID id,
        String name,
        AssetCategory category,
        BigDecimal currentValue,
        String currency,
        LocalDate valuationDate,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {
}
