package finadvisor.networth.service;

import finadvisor.networth.dto.AssetRequest;
import finadvisor.networth.dto.AssetResponse;
import finadvisor.networth.dto.LiabilityRequest;
import finadvisor.networth.dto.LiabilityResponse;
import finadvisor.networth.dto.NetWorthHistoryResponse;
import finadvisor.networth.dto.NetWorthSummaryResponse;

import java.util.List;
import java.util.UUID;

public interface NetWorthService {

    NetWorthSummaryResponse getSummary(String userEmail);

    NetWorthHistoryResponse getHistory(String userEmail);

    List<AssetResponse> listAssets(String userEmail);

    AssetResponse createAsset(String userEmail, AssetRequest request);

    AssetResponse updateAsset(String userEmail, UUID assetId, AssetRequest request);

    void deleteAsset(String userEmail, UUID assetId);

    List<LiabilityResponse> listLiabilities(String userEmail);

    LiabilityResponse createLiability(String userEmail, LiabilityRequest request);

    LiabilityResponse updateLiability(String userEmail, UUID liabilityId, LiabilityRequest request);

    void deleteLiability(String userEmail, UUID liabilityId);
}
