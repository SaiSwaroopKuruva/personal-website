package finadvisor.stock.entity;

/** Mirrors {@link finadvisor.marketdata.DataType} but persisted as plain text on {@code stock_prices}. */
public enum PriceDataType {
    INTRADAY,
    END_OF_DAY
}
