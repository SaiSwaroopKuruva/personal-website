package finadvisor.marketdata;

/** Response body for {@code GET /api/data-providers/status} (Part 30). */
public record ProviderStatusResponse(ProviderStatus stocks, ProviderStatus mutualFunds) {
}
