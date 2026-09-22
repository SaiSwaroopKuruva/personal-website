package finadvisor.mutualfund.provider.amfi;

import java.math.BigDecimal;
import java.time.LocalDate;

/** One data row parsed from AMFI's published NAVAll.txt file, plus the AMC/category context it appeared under. */
public record AmfiSchemeRecord(
        String schemeCode,
        String isinGrowth,
        String isinReinvestment,
        String schemeName,
        BigDecimal nav,
        LocalDate navDate,
        String amcName,
        String schemeCategoryLine
) {
    public String isin() {
        if (isinGrowth != null && !isinGrowth.isBlank()) {
            return isinGrowth;
        }
        if (isinReinvestment != null && !isinReinvestment.isBlank()) {
            return isinReinvestment;
        }
        return null;
    }
}
