package finadvisor.portfolio.scheduler;

import finadvisor.portfolio.service.PortfolioSnapshotService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Daily valuation snapshot job (Part 16). Render's free/hobby tiers can sleep, restart or - on some plans -
 * run more than one instance; this in-process {@code @Scheduled} job is therefore a best-effort convenience,
 * not a guarantee. {@link PortfolioSnapshotService#captureSnapshot} is idempotent per (portfolio, date), so
 * a missed run, a restart mid-run, or an operator manually re-triggering the job never produces duplicates
 * or corrupts history. Operationally: if reliable daily snapshots are required in production, trigger this
 * via an external scheduler (e.g. a Render Cron Job or GitHub Actions workflow calling a protected endpoint)
 * rather than relying solely on in-process {@code @Scheduled} - documented as a known limitation (Part 28).
 */
@Component
public class PortfolioSnapshotScheduler {

    private static final Logger log = Logger.getLogger(PortfolioSnapshotScheduler.class.getName());

    private final PortfolioSnapshotService snapshotService;

    public PortfolioSnapshotScheduler(PortfolioSnapshotService snapshotService) {
        this.snapshotService = snapshotService;
    }

    @Scheduled(cron = "${portfolio.snapshot.cron:-}")
    public void runDailySnapshot() {
        try {
            snapshotService.captureAllPortfolios();
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Scheduled portfolio snapshot run failed", ex);
        }
    }
}
