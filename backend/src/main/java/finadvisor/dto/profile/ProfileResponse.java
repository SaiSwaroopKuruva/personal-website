package finadvisor.dto.profile;

import finadvisor.entity.Gender;
import finadvisor.entity.InvestmentExperience;
import finadvisor.entity.InvestmentHorizon;
import finadvisor.entity.KycStatus;
import finadvisor.entity.RiskProfile;
import finadvisor.entity.UserStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ProfileResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String mobile,
        RiskProfile riskProfile,
        LocalDate dateOfBirth,
        Gender gender,
        String occupation,
        BigDecimal annualIncome,
        BigDecimal monthlyExpenses,
        String city,
        String state,
        String country,
        String postalCode,
        String panNumber,
        String aadhaarLastFour,
        KycStatus kycStatus,
        boolean emailVerified,
        boolean mobileVerified,
        boolean profileCompleted,
        String preferredLanguage,
        InvestmentExperience investmentExperience,
        InvestmentHorizon investmentHorizon,
        BigDecimal monthlyInvestmentBudget,
        String profilePicture,
        Instant lastLogin,
        UserStatus status,
        Instant createdAt
) {
}
