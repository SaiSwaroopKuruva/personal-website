package finadvisor.stock.provider;

import finadvisor.marketdata.ErrorCategory;

/** Signals a failure while communicating with an external stock market-data provider. */
public class StockProviderException extends RuntimeException {

    private final ErrorCategory category;

    public StockProviderException(String message, ErrorCategory category) {
        super(message);
        this.category = category;
    }

    public StockProviderException(String message, ErrorCategory category, Throwable cause) {
        super(message, cause);
        this.category = category;
    }

    public ErrorCategory getCategory() {
        return category;
    }

    public boolean isRetryable() {
        return category == ErrorCategory.TIMEOUT
                || category == ErrorCategory.PROVIDER_UNAVAILABLE
                || category == ErrorCategory.RATE_LIMITED;
    }
}
