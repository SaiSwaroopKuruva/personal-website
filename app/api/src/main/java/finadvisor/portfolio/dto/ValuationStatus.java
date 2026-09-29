package finadvisor.portfolio.dto;

/** Whether a valuation can be trusted right now (Part 10) - never silently treated as zero or "live". */
public enum ValuationStatus {
    CURRENT,
    STALE,
    UNAVAILABLE
}
