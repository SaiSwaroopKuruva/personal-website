package finadvisor.dto.mutualfund;

import java.math.BigDecimal;
import java.time.LocalDate;

/** {@code annualized=true} means {@link #returnPercentage} is a CAGR; otherwise it is an absolute/point-to-point return. */
public record ReturnResponse(String period, BigDecimal returnPercentage, boolean annualized, LocalDate calculatedAsOf) {
}
