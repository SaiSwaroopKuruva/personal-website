package finadvisor.dto.mutualfund;

import java.math.BigDecimal;

public record ComparisonFundResponse(
        String schemeCode,
        String schemeName,
        String amcName,
        String category,
        String subCategory,
        String riskLevel,
        BigDecimal nav,
        BigDecimal aum,
        BigDecimal expenseRatio,
        BigDecimal oneYearReturn,
        BigDecimal threeYearReturn,
        BigDecimal fiveYearReturn,
        BigDecimal sinceInceptionReturn,
        BigDecimal minimumSip,
        BigDecimal minimumLumpsum,
        String exitLoad,
        String benchmark,
        String fundManager
) {
}
