package finadvisor.mutualfund.service;

import finadvisor.mutualfund.dto.SyncResultResponse;

import java.util.List;

/**
 * Orchestrates pulling data from the configured {@code MutualFundDataProvider} and upserting it into the
 * platform's own tables (Part 29). The search/details/etc. APIs always read from our database, never
 * directly from the provider, keeping the provider swappable and the read path fast and provider-outage-safe.
 */
public interface MutualFundDataSyncService {

    SyncResultResponse syncFunds();

    SyncResultResponse syncNav();

    SyncResultResponse syncHoldings();

    SyncResultResponse syncReturns();

    SyncResultResponse syncManagers();

    List<SyncResultResponse> syncAll();
}
