package finadvisor.mutualfund.entity;

/** SEBI riskometer-style risk bands for a mutual fund scheme (distinct from the investor {@link RiskLevel}). */
public enum FundRiskLevel {
    LOW,
    LOW_TO_MODERATE,
    MODERATE,
    MODERATELY_HIGH,
    HIGH,
    VERY_HIGH,
    /** Provider (e.g. AMFI's daily NAV file) does not publish a SEBI riskometer classification for this scheme. */
    NOT_RATED
}
