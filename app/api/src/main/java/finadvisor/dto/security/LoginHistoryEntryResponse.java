package finadvisor.dto.security;

import java.time.Instant;
import java.util.UUID;

public record LoginHistoryEntryResponse(
        UUID id,
        String action,
        String ipAddress,
        Instant createdAt
) {
}
