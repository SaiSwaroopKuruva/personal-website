package finadvisor.mutualfund.service;

import finadvisor.dto.PageResponse;
import finadvisor.mutualfund.dto.FilterMetadataResponse;
import finadvisor.mutualfund.dto.FundManagerResponse;
import finadvisor.mutualfund.dto.HoldingResponse;
import finadvisor.mutualfund.dto.MutualFundDetailsResponse;
import finadvisor.mutualfund.dto.MutualFundSummaryResponse;
import finadvisor.mutualfund.dto.NavHistoryResponse;
import finadvisor.mutualfund.dto.ReturnsResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface MutualFundService {

    PageResponse<MutualFundSummaryResponse> search(String search, String amc, String category, String subCategory,
                                                     String planType, String optionType, String riskLevel,
                                                     BigDecimal minAum, BigDecimal maxAum, BigDecimal minExpenseRatio,
                                                     BigDecimal maxExpenseRatio, String sort, String direction,
                                                     int page, int size, String currentUserEmail);

    MutualFundDetailsResponse getDetails(String schemeCode, String currentUserEmail);

    NavHistoryResponse getNavHistory(String schemeCode, LocalDate from, LocalDate to, String interval);

    ReturnsResponse getReturns(String schemeCode);

    PageResponse<HoldingResponse> getHoldings(String schemeCode, String sector, String assetType, LocalDate asOfDate, int page, int size);

    List<FundManagerResponse> getManagers(String schemeCode);

    FilterMetadataResponse getFilterMetadata();
}
