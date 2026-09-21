package finadvisor.mutualfund.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "mutualfund.provider")
@Getter
@Setter
public class MutualFundProviderProperties {

    /** Which {@code MutualFundDataProvider} bean to use, matched against its {@code @ConditionalOnProperty} value. */
    private String name = "demo";

    /** Base URL of a real external provider. Left blank for the built-in demo provider. */
    private String baseUrl;

    /** API key/secret for a real provider. Never logged, never exposed to the frontend. */
    private String apiKey;

    private int timeoutMs = 5000;

    private int maxRetries = 3;

    /** Whether the scheduled/admin-triggered sync job is allowed to run. */
    private boolean enabled = false;
}
