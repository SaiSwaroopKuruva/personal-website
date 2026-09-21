package finadvisor.mutualfund.provider;

import java.time.LocalDate;
import java.util.List;

/**
 * Abstraction over an external mutual-fund market-data source. The application depends only on this
 * interface (never on a specific vendor), so a real provider (e.g. AMFI, MFApi.in, a paid data vendor)
 * can be plugged in later by adding a new implementation and pointing {@code mutualfund.provider.name}
 * at it - no changes needed in the search/details/sync APIs. Implementations must:
 * <ul>
 *   <li>never throw for expected "not found" results (wrap in {@link ProviderResponse#failure})</li>
 *   <li>only throw {@link ProviderException} for transport/parsing failures, with {@code retryable} set
 *       appropriately so callers can apply retry/backoff only where it is safe to do so</li>
 *   <li>never expose provider-specific types to controllers - only the records in this package</li>
 * </ul>
 */
public interface MutualFundDataProvider {

    String getProviderName();

    ProviderResponse<List<ProviderFundData>> searchFunds(String query);

    ProviderResponse<ProviderFundData> getFundDetails(String schemeCode);

    ProviderResponse<ProviderNavPoint> getCurrentNav(String schemeCode);

    ProviderResponse<List<ProviderNavPoint>> getHistoricalNav(String schemeCode, LocalDate from, LocalDate to);

    ProviderResponse<List<ProviderHoldingData>> getHoldings(String schemeCode);

    ProviderResponse<List<ProviderManagerData>> getFundManagers(String schemeCode);

    ProviderResponse<List<ProviderReturnData>> getFundReturns(String schemeCode);

    /** Full catalog of funds the provider currently publishes; used by the nightly/full sync job. */
    ProviderResponse<List<ProviderFundData>> syncFunds();
}
