package finadvisor.entity;

/** SEBI riskometer-style risk bands for a mutual fund scheme (distinct from the investor {@link RiskLevel}). */
public enum FundRiskLevel {
    LOW,
    LOW_TO_MODERATE,
    MODERATE,
    MODERATELY_HIGH,
    HIGH,
    VERY_HIGH
}
