package finadvisor.stock.dto;

import java.time.Instant;

public record NewsItemResponse(String heading, String summary, String articleLink, Instant publishedAt) {
}
