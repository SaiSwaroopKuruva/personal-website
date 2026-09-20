package finadvisor.dto.auth;

import finadvisor.entity.RiskProfile;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String mobile,
        RiskProfile riskProfile,
        Instant createdAt
) {
}
