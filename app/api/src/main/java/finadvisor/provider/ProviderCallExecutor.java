package finadvisor.provider;

import finadvisor.config.MutualFundProviderProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Executes a provider call with exponential-backoff retry (Part 6). Only {@link ProviderException}s
 * marked {@code retryable} are retried; everything else (including expected "not found" results, which
 * providers surface via {@link ProviderResponse#failure}) is returned/propagated immediately.
 */
@Component
@RequiredArgsConstructor
public class ProviderCallExecutor {

    private static final Logger log = Logger.getLogger(ProviderCallExecutor.class.getName());

    private final MutualFundProviderProperties properties;

    public <T> T executeWithRetry(String operation, Supplier<T> call) {
        int attempt = 0;
        long delayMs = 500L;
        while (true) {
            attempt++;
            try {
                return call.get();
            } catch (ProviderException ex) {
                boolean canRetry = ex.isRetryable() && attempt < properties.getMaxRetries();
                int attemptNumber = attempt;
                log.log(Level.WARNING, () -> "Provider call '" + operation + "' failed on attempt " + attemptNumber + "/"
                        + properties.getMaxRetries() + " (retryable=" + ex.isRetryable() + "): " + ex.getMessage());
                if (!canRetry) {
                    throw ex;
                }
                sleep(delayMs);
                delayMs *= 2;
            }
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ProviderException("Interrupted while waiting to retry provider call", e, false);
        }
    }
}
