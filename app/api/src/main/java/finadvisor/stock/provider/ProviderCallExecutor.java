package finadvisor.stock.provider;

import finadvisor.marketdata.ErrorCategory;
import finadvisor.stock.config.StockProviderProperties;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Executes a provider call with exponential-backoff retry (Part 16/6) - only retryable {@link StockProviderException}s are retried. */
@Component
public class ProviderCallExecutor {

    private static final Logger log = Logger.getLogger(ProviderCallExecutor.class.getName());

    private final StockProviderProperties properties;

    public ProviderCallExecutor(StockProviderProperties properties) {
        this.properties = properties;
    }

    public <T> T executeWithRetry(String operation, Supplier<T> call) {
        int attempt = 0;
        long delayMs = 500L;
        while (true) {
            attempt++;
            try {
                return call.get();
            } catch (StockProviderException ex) {
                boolean canRetry = ex.isRetryable() && attempt < properties.getMaxRetries();
                int attemptNumber = attempt;
                log.log(Level.WARNING, () -> "Provider call '" + operation + "' failed on attempt " + attemptNumber + "/"
                        + properties.getMaxRetries() + " (category=" + ex.getCategory() + "): " + ex.getMessage());
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
            throw new StockProviderException("Interrupted while waiting to retry provider call", ErrorCategory.UNKNOWN, e);
        }
    }
}
