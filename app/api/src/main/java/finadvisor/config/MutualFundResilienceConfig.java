package finadvisor.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Enables {@code @Scheduled} jobs (mutual fund data sync, Part 29). Retry/backoff for provider calls is
 * implemented explicitly in {@code finadvisor.provider.ProviderCallExecutor} rather than via AOP. */
@Configuration
@EnableScheduling
public class MutualFundResilienceConfig {
}
