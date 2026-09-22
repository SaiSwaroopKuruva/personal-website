package finadvisor.marketdata;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory, process-local health tracker shared by every financial-data provider. Providers report
 * successes/failures here; {@code ProviderStatusController} reads it back for {@code GET /api/data-providers/status}.
 * Deliberately holds no credentials, request payloads, or stack traces - only provider name, timestamps and
 * a short human-readable message.
 */
@Component
public class ProviderHealthTracker {

    private final Map<String, ProviderStatus> statusByKey = new ConcurrentHashMap<>();

    public void recordSuccess(String key, String providerName, DataType dataType) {
        statusByKey.put(key, new ProviderStatus(providerName, ProviderConnectionStatus.CONNECTED, Instant.now(), dataType, null));
    }

    public void recordFailure(String key, String providerName, String safeMessage) {
        ProviderStatus previous = statusByKey.get(key);
        Instant lastSuccess = previous != null ? previous.lastSuccessfulUpdate() : null;
        DataType dataType = previous != null ? previous.dataType() : null;
        statusByKey.put(key, new ProviderStatus(providerName, ProviderConnectionStatus.DEGRADED, lastSuccess, dataType, safeMessage));
    }

    public void recordDisabled(String key, String providerName, String reason) {
        statusByKey.put(key, new ProviderStatus(providerName, ProviderConnectionStatus.DISABLED, null, null, reason));
    }

    public ProviderStatus getStatus(String key, String fallbackProviderName) {
        return statusByKey.getOrDefault(key,
                new ProviderStatus(fallbackProviderName, ProviderConnectionStatus.UNAVAILABLE, null, null, "No successful call yet"));
    }
}
