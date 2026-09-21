package finadvisor.mutualfund.provider;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProviderNavPoint(String schemeCode, BigDecimal nav, LocalDate navDate) {
}
