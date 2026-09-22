package finadvisor.marketdata;

/**
 * Standardized exception for failures talking to an external financial-data provider (Upstox, AMFI, or any
 * future provider). {@link #category} drives both HTTP status mapping ({@code GlobalExceptionHandler}) and
 * retry decisions - only {@link ErrorCategory#TIMEOUT}, {@link ErrorCategory#PROVIDER_UNAVAILABLE} and
 * {@link ErrorCategory#RATE_LIMITED} are ever retried. Never carries provider credentials or raw
 * authorization headers in its message.
 */
public class FinancialDataProviderException extends RuntimeException {

    private final ErrorCategory category;
    private final String provider;

    public FinancialDataProviderException(String provider, ErrorCategory category, String message) {
        super(message);
        this.provider = provider;
        this.category = category;
    }

    public FinancialDataProviderException(String provider, ErrorCategory category, String message, Throwable cause) {
        super(message, cause);
        this.provider = provider;
        this.category = category;
    }

    public ErrorCategory getCategory() {
        return category;
    }

    public String getProvider() {
        return provider;
    }

    public boolean isRetryable() {
        return category == ErrorCategory.TIMEOUT
                || category == ErrorCategory.PROVIDER_UNAVAILABLE
                || category == ErrorCategory.RATE_LIMITED;
    }
}
