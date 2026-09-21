package finadvisor.mutualfund.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MutualFundSummaryResponse(
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
        String riskLevel,
        BigDecimal nav,
        LocalDate navDate,
        BigDecimal expenseRatio,
        BigDecimal aum,
        BigDecimal minimumSip,
        BigDecimal minimumLumpsum,
        BigDecimal oneYearReturn,
        BigDecimal threeYearReturn,
        BigDecimal fiveYearReturn,
        boolean favorite
) {
}
