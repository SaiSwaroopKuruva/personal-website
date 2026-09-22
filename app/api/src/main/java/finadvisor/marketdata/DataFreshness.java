package finadvisor.marketdata;

/** How current a returned data point is relative to its source, independent of {@link DataType}. */
public enum DataFreshness {
    LIVE,
    FRESH,
    STALE,
    UNKNOWN
}
