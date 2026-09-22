package finadvisor.marketdata;

/** Standardized failure categories for any external financial-data provider (stocks or mutual funds). */
public enum ErrorCategory {
    AUTHENTICATION_ERROR,
    AUTHORIZATION_ERROR,
    RATE_LIMITED,
    PROVIDER_UNAVAILABLE,
    INVALID_REQUEST,
    INVALID_PROVIDER_RESPONSE,
    TIMEOUT,
    DATA_VALIDATION_ERROR,
    UNSUPPORTED_OPERATION,
    UNKNOWN
}
