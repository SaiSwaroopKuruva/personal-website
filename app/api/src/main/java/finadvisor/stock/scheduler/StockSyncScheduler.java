package finadvisor.stock.scheduler;

import finadvisor.stock.config.StockProviderProperties;
import finadvisor.stock.service.StockDataSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Scheduled stock data sync (Part 26). Disabled by default via {@code stock.provider.enabled=false} and the
 * "-" (never fire) cron defaults, so a misconfigured/unset provider never runs automatically in production.
 */
@Component
@RequiredArgsConstructor
public class StockSyncScheduler {

    private static final Logger log = Logger.getLogger(StockSyncScheduler.class.getName());

    private final StockDataSyncService syncService;
    private final StockProviderProperties providerProperties;

    @Scheduled(cron = "${stock.sync.instrument-cron:-}")
    public void runInstrumentSync() {
        if (!providerProperties.isEnabled()) {
            return;
        }
        runSafely(syncService::syncInstruments);
    }

    @Scheduled(cron = "${stock.sync.eod-cron:-}")
    public void runEodSync() {
        if (!providerProperties.isEnabled()) {
            return;
        }
        runSafely(syncService::syncEndOfDayPrices);
    }

    private void runSafely(Runnable job) {
        try {
            job.run();
        } catch (Exception ex) {
            // A provider outage must never crash the application or block other scheduled jobs.
            log.log(Level.SEVERE, "Scheduled stock sync failed", ex);
        }
    }
}
