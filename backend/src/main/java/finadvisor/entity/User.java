package finadvisor.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, unique = true, length = 20)
    private String mobile;

    @Column(nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_profile", nullable = false, length = 20)
    private RiskProfile riskProfile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Gender gender;

    @Column(length = 100)
    private String occupation;

    @Column(name = "annual_income", precision = 15, scale = 2)
    private BigDecimal annualIncome;

    @Column(name = "monthly_expenses", precision = 15, scale = 2)
    private BigDecimal monthlyExpenses;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(length = 100)
    private String country;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "pan_number", length = 10)
    private String panNumber;

    @Column(name = "aadhaar_last_four", length = 4)
    private String aadhaarLastFour;

    @Enumerated(EnumType.STRING)
    @Column(name = "kyc_status", nullable = false, length = 20)
    private KycStatus kycStatus;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @Column(name = "mobile_verified", nullable = false)
    private boolean mobileVerified;

    @Column(name = "profile_completed", nullable = false)
    private boolean profileCompleted;

    @Column(name = "preferred_language", nullable = false, length = 10)
    private String preferredLanguage;

    @Enumerated(EnumType.STRING)
    @Column(name = "investment_experience", length = 20)
    private InvestmentExperience investmentExperience;

    @Enumerated(EnumType.STRING)
    @Column(name = "investment_horizon", length = 20)
    private InvestmentHorizon investmentHorizon;

    @Column(name = "monthly_investment_budget", precision = 15, scale = 2)
    private BigDecimal monthlyInvestmentBudget;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "notification_preferences", nullable = false, columnDefinition = "jsonb")
    private NotificationPreferences notificationPreferences;

    @Column(name = "profile_picture", length = 500)
    private String profilePicture;

    @Column(name = "last_login")
    private Instant lastLogin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.riskProfile == null) {
            this.riskProfile = RiskProfile.MODERATE;
        }
        if (this.role == null) {
            this.role = Role.USER;
        }
        if (this.kycStatus == null) {
            this.kycStatus = KycStatus.PENDING;
        }
        if (this.preferredLanguage == null) {
            this.preferredLanguage = "en";
        }
        if (this.notificationPreferences == null) {
            this.notificationPreferences = NotificationPreferences.defaults();
        }
        if (this.status == null) {
            this.status = UserStatus.ACTIVE;
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
