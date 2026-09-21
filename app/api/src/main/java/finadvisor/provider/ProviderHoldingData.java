package finadvisor.provider;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProviderHoldingData(
        String securityName,
        String isin,
        String sector,
        String assetType,
        BigDecimal weightPercentage,
        BigDecimal quantity,
        BigDecimal marketValue,
        LocalDate asOfDate
) {
}
