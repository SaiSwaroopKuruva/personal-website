package finadvisor.stock.dto;

import java.time.Instant;

public record MarketStatusResponse(String exchange, String status, Instant lastUpdated) {
}
