package finadvisor.dto.profile;

import finadvisor.entity.AddressType;

import java.time.Instant;
import java.util.UUID;

public record AddressResponse(
        UUID id,
        AddressType type,
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        String country,
        String postalCode,
        boolean isDefault,
        Instant createdAt,
        Instant updatedAt
) {
}
