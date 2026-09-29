package finadvisor.portfolio.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** One reproducible valuation per portfolio per date (Part 4.4/16); never overwrites transaction history. */
@Entity
@Table(name = "portfolio_valuation_snapshots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(of = {"id", "snapshotDate"})
public class PortfolioValuationSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "invested_value", nullable = false, precision = 18, scale = 2)
    private BigDecimal investedValue;

    @Column(name = "market_value", nullable = false, precision = 18, scale = 2)
    private BigDecimal marketValue;

    @Column(name = "realized_gain", nullable = false, precision = 18, scale = 2)
    private BigDecimal realizedGain;

    @Column(name = "unrealized_gain", nullable = false, precision = 18, scale = 2)
    private BigDecimal unrealizedGain;

    @Column(name = "is_complete", nullable = false)
    private boolean complete;

    @Column(name = "calculated_at", nullable = false)
    private Instant calculatedAt;

    @PrePersist
    void onCreate() {
        if (this.calculatedAt == null) {
            this.calculatedAt = Instant.now();
        }
    }
}
