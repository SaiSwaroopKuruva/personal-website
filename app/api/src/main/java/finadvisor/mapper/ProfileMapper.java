package finadvisor.mapper;

import finadvisor.dto.profile.ProfileResponse;
import finadvisor.entity.User;
import org.springframework.stereotype.Component;

@Component
public class ProfileMapper {

    public ProfileResponse toResponse(User user) {
        return new ProfileResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getMobile(),
                user.getRiskProfile(),
                user.getDateOfBirth(),
                user.getGender(),
                user.getOccupation(),
                user.getAnnualIncome(),
                user.getMonthlyExpenses(),
                user.getCity(),
                user.getState(),
                user.getCountry(),
                user.getPostalCode(),
                user.getPanNumber(),
                user.getAadhaarLastFour(),
                user.getKycStatus(),
                user.isEmailVerified(),
                user.isMobileVerified(),
                user.isProfileCompleted(),
                user.getPreferredLanguage(),
                user.getInvestmentExperience(),
                user.getInvestmentHorizon(),
                user.getMonthlyInvestmentBudget(),
                user.getProfilePicture(),
                user.getLastLogin(),
                user.getStatus(),
                user.getCreatedAt());
    }
}
