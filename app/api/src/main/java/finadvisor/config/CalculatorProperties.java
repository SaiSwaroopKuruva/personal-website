package finadvisor.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/** Upper-bound guardrails for the SIP/lumpsum/SWP calculators (Part 19) - reject unreasonable inputs outright. */
@Configuration
@ConfigurationProperties(prefix = "mutualfund.calculator")
@Getter
@Setter
public class CalculatorProperties {

    private BigDecimal maxAmount = new BigDecimal("100000000");

    private int maxDurationYears = 50;

    private BigDecimal maxAnnualReturnPercentage = new BigDecimal("30");
}
