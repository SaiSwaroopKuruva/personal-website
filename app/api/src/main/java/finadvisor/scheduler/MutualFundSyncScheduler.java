package finadvisor.scheduler;

import finadvisor.config.MutualFundProviderProperties;
import finadvisor.service.MutualFundDataSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Scheduled mutual fund data sync (Part 29). Disabled by default: {@code mutualfund.sync.cron=-} (Spring's
 * documented "never fire" marker) unless overridden, and gated by {@code mutualfund.provider.enabled} so a
 * misconfigured/unset provider never runs automatically in production.
 */
@Component
@RequiredArgsConstructor
public class MutualFundSyncScheduler {

    private static final Logger log = Logger.getLogger(MutualFundSyncScheduler.class.getName());

    private final MutualFundDataSyncService syncService;
    private final MutualFundProviderProperties providerProperties;

    @Scheduled(cron = "${mutualfund.sync.cron:-}")
    public void runScheduledSync() {
        if (!providerProperties.isEnabled()) {
            log.fine("Skipping scheduled mutual fund sync: mutualfund.provider.enabled=false");
            return;
        }
        try {
            syncService.syncAll();
        } catch (Exception ex) {
            // A provider outage must never crash the application or block other scheduled jobs.
            log.log(Level.SEVERE, "Scheduled mutual fund sync failed", ex);
        }
    }
}
