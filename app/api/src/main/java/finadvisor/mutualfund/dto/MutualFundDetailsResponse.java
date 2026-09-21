package finadvisor.mutualfund.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record MutualFundDetailsResponse(
        String schemeCode,
        String isin,
        String schemeName,
        String shortName,
        String amcCode,
        String amcName,
        String category,
        String subCategory,
        String planType,
        String optionType,
        String assetClass,
        String investmentObjective,
        String riskLevel,
        String benchmark,
        BigDecimal expenseRatio,
        String exitLoad,
        BigDecimal minimumLumpsum,
        BigDecimal minimumSip,
        BigDecimal aum,
        BigDecimal nav,
        LocalDate navDate,
        LocalDate inceptionDate,
        String fundManager,
        List<FundManagerResponse> managers,
        List<ReturnResponse> returns,
        List<HoldingResponse> topHoldings,
        LocalDate holdingsAsOfDate,
        boolean favorite,
        String disclaimer
) {
}
