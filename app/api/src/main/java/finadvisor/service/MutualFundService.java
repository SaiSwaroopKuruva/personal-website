package finadvisor.service;

import finadvisor.dto.PageResponse;
import finadvisor.dto.mutualfund.FilterMetadataResponse;
import finadvisor.dto.mutualfund.FundManagerResponse;
import finadvisor.dto.mutualfund.HoldingResponse;
import finadvisor.dto.mutualfund.MutualFundDetailsResponse;
import finadvisor.dto.mutualfund.MutualFundSummaryResponse;
import finadvisor.dto.mutualfund.NavHistoryResponse;
import finadvisor.dto.mutualfund.ReturnsResponse;

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
