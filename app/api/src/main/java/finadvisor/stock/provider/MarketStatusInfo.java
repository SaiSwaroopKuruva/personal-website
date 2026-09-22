package finadvisor.stock.provider;

/** Exchange-level trading status (Part 5/19), e.g. NSE = NORMAL_OPEN, PRE_OPEN, CLOSED. */
public record MarketStatusInfo(String exchange, String status, java.time.Instant lastUpdated) {
}
