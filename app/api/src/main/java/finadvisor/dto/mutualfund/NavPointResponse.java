package finadvisor.dto.mutualfund;

import java.math.BigDecimal;
import java.time.LocalDate;

public record NavPointResponse(LocalDate date, BigDecimal nav) {
}
