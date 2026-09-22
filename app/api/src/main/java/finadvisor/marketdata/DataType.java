package finadvisor.marketdata;

/** Classifies how a piece of financial data was sourced - never mark data with a stronger guarantee than the provider actually gave. */
public enum DataType {
    REAL_TIME,
    INTRADAY,
    DELAYED,
    END_OF_DAY,
    LATEST_NAV,
    HISTORICAL_NAV
}
