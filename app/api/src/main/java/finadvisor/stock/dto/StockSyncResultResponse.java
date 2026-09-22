package finadvisor.stock.dto;

import java.time.Instant;

public record StockSyncResultResponse(
        String syncType,
        Instant startedAt,
        Instant completedAt,
        int recordsProcessed,
        int recordsFailed,
        String status,
        String errorMessage
) {
}
