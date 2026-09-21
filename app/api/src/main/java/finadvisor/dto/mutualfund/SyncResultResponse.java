package finadvisor.dto.mutualfund;

import java.time.Instant;

public record SyncResultResponse(
        String provider,
        String syncType,
        String status,
        int recordsProcessed,
        int recordsFailed,
        Instant startedAt,
        Instant completedAt,
        String errorMessage
) {
}
