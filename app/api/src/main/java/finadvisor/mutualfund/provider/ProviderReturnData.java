package finadvisor.mutualfund.provider;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProviderReturnData(String period, BigDecimal returnPercentage, boolean annualized, LocalDate calculatedAsOf) {
}
