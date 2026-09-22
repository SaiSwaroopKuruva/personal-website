package finadvisor.stock.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** Generic stock market-data provider configuration - which provider bean to use and its retry/timeout envelope. */
@Configuration
@ConfigurationProperties(prefix = "stock.provider")
@Getter
@Setter
public class StockProviderProperties {

    /** Which {@code StockMarketDataProvider} bean to use, matched against its {@code @ConditionalOnProperty} value. */
    private String name = "none";

    private int timeoutMs = 5000;

    private int maxRetries = 3;

    /** Whether the scheduled/admin-triggered stock sync job is allowed to run. */
    private boolean enabled = false;
}
