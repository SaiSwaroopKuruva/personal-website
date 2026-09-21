package finadvisor.mutualfund.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
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

@Entity
@Table(name = "mutual_funds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(of = {"id", "schemeCode", "schemeName"})
public class MutualFund {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "scheme_code", nullable = false, unique = true, length = 50)
    private String schemeCode;

    @Column(length = 20)
    private String isin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "amc_id", nullable = false)
    private MutualFundAmc amc;

    @Column(name = "scheme_name", nullable = false, length = 300)
    private String schemeName;

    @Column(name = "short_name", length = 150)
    private String shortName;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(name = "sub_category", length = 100)
    private String subCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", nullable = false, length = 20)
    private PlanType planType;

    @Enumerated(EnumType.STRING)
    @Column(name = "option_type", nullable = false, length = 20)
    private OptionType optionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_class", nullable = false, length = 50)
    private AssetClass assetClass;

    @Column(name = "investment_objective", length = 2000)
    private String investmentObjective;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false, length = 30)
    private FundRiskLevel riskLevel;

    @Column(length = 200)
    private String benchmark;

    @Column(name = "expense_ratio", precision = 5, scale = 2)
    private BigDecimal expenseRatio;

    @Column(name = "exit_load", length = 500)
    private String exitLoad;

    @Column(name = "minimum_lumpsum", precision = 12, scale = 2)
    private BigDecimal minimumLumpsum;

    @Column(name = "minimum_sip", precision = 12, scale = 2)
    private BigDecimal minimumSip;

    @Column(precision = 18, scale = 2)
    private BigDecimal aum;

    @Column(precision = 12, scale = 4)
    private BigDecimal nav;

    @Column(name = "nav_date")
    private LocalDate navDate;

    @Column(name = "inception_date")
    private LocalDate inceptionDate;

    @Column(name = "fund_manager", length = 200)
    private String fundManager;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FundStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = FundStatus.ACTIVE;
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
