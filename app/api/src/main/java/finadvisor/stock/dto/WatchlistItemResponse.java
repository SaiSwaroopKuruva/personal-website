package finadvisor.stock.dto;

import java.time.Instant;

/** A single watchlist entry (Part 21) - quote fields are best-effort and may be null if the provider call failed. */
public record WatchlistItemResponse(
        String symbol,
        String companyName,
        String exchange,
        StockQuoteResponse quote,
        Instant addedAt
) {
}
