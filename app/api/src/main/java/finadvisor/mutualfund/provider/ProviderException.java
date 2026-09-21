package finadvisor.mutualfund.provider;

/**
 * Signals a failure while communicating with an external mutual-fund market-data provider.
 * {@link #retryable} distinguishes transient failures (timeouts, 5xx, connection resets) - which the
 * caller may retry with backoff - from non-retryable failures (4xx, malformed data) which must not be retried.
 */
public class ProviderException extends RuntimeException {

    private final boolean retryable;

    public ProviderException(String message, boolean retryable) {
        super(message);
        this.retryable = retryable;
    }

    public ProviderException(String message, Throwable cause, boolean retryable) {
        super(message, cause);
        this.retryable = retryable;
    }

    public boolean isRetryable() {
        return retryable;
    }
}
