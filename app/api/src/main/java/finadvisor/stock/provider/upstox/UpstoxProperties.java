package finadvisor.stock.provider.upstox;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Upstox-specific configuration (Part 7). {@code analyticsToken} is the long-lived, read-only Analytics
 * Token (see https://upstox.com/developer/api-documentation/analytics-token) - it must only ever be read
 * from an environment variable, never logged, never returned in any API response, and never placed in a
 * {@code NEXT_PUBLIC_*} frontend variable.
 */
@Configuration
@ConfigurationProperties(prefix = "stock.provider.upstox")
@Getter
@Setter
public class UpstoxProperties {

    private String apiBaseUrl = "https://api.upstox.com";

    /** Read-only Analytics Token - server-side only, sourced from UPSTOX_ANALYTICS_TOKEN. */
    private String analyticsToken;

    /**
     * Bounded universe of NSE trading symbols this deployment tracks/syncs (Part 40 - never fetch the
     * entire exchange). Upstox has no free-text stock screener API, so "top gainers/losers/most active"
     * are computed locally from real quotes across exactly this list (see docs/financial-data-providers.md).
     */
    private List<String> trackedSymbols = List.of(
            "RELIANCE", "TCS", "HDFCBANK", "INFY", "ICICIBANK", "SBIN", "ITC", "HINDUNILVR",
            "BHARTIARTL", "KOTAKBANK", "LT", "AXISBANK", "BAJFINANCE", "MARUTI", "ASIANPAINT",
            "TITAN", "SUNPHARMA", "ULTRACEMCO", "WIPRO", "HCLTECH");

    /** Instrument keys for the indices shown on the market overview (Full Market Quote V3 supports NSE_INDEX). */
    private List<String> indexInstrumentKeys = List.of(
            "NSE_INDEX|Nifty 50", "NSE_INDEX|Nifty Bank", "NSE_INDEX|Nifty IT", "NSE_INDEX|Nifty Midcap 100");

    private int cacheTtlMinutes = 30;
}
