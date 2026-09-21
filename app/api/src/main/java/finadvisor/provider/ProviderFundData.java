package finadvisor.provider;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Provider-shaped fund master data. Never exposed directly to controllers - always mapped into internal DTOs/entities. */
public record ProviderFundData(
        String schemeCode,
        String isin,
        String amcCode,
        String amcName,
        String schemeName,
        String shortName,
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
        String fundManager
) {
}
