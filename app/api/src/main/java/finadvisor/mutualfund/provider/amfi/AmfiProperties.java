package finadvisor.mutualfund.provider.amfi;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * AMFI-specific configuration, separate from the generic {@link finadvisor.mutualfund.config.MutualFundProviderProperties}
 * because AMFI is a fixed public data source (a published text file), not a keyed/authenticated API.
 */
@Configuration
@ConfigurationProperties(prefix = "mutualfund.provider.amfi")
@Getter
@Setter
public class AmfiProperties {

    /**
     * URL of AMFI's published daily "NAVAll" file - a plain-text, semicolon-separated snapshot of every
     * active scheme's latest NAV, grouped by AMC and category. This is AMFI's only documented free/public
     * bulk NAV feed; there is no documented per-scheme historical NAV REST API (see docs/financial-data-providers.md).
     */
    private String navAllUrl = "https://www.amfiindia.com/spragmt/NAVAll.txt";

    /** How long the in-memory parsed snapshot is reused before re-fetching from AMFI, to avoid hammering the source. */
    private int cacheTtlMinutes = 60;
}
