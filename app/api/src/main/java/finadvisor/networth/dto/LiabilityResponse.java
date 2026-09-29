package finadvisor.networth.dto;

import finadvisor.networth.entity.LiabilityCategory;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record LiabilityResponse(
        UUID id,
        String name,
        LiabilityCategory category,
        BigDecimal currentValue,
        String currency,
        LocalDate valuationDate,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {
}
