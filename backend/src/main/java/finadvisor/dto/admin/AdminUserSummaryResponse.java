package finadvisor.dto.admin;

import finadvisor.entity.KycStatus;
import finadvisor.entity.UserStatus;

import java.time.Instant;
import java.util.UUID;

public record AdminUserSummaryResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String mobile,
        UserStatus status,
        KycStatus kycStatus,
        boolean emailVerified,
        Instant createdAt
) {
}
