package finadvisor.marketdata;

import java.time.Instant;

/** Public-safe snapshot of a provider's health - never includes credentials or internal error detail. */
public record ProviderStatus(
        String provider,
        ProviderConnectionStatus status,
        Instant lastSuccessfulUpdate,
        DataType dataType,
        String message
) {
}
