package finadvisor.stock.provider;

import java.time.Instant;

public record NewsItem(String heading, String summary, String articleLink, Instant publishedAt) {
}
