package finadvisor.mutualfund.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record HoldingResponse(
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
