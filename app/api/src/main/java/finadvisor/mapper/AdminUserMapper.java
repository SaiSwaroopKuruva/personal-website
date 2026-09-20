package finadvisor.mapper;

import finadvisor.dto.admin.AdminUserSummaryResponse;
import finadvisor.entity.User;
import org.springframework.stereotype.Component;

@Component
public class AdminUserMapper {

    public AdminUserSummaryResponse toSummaryResponse(User user) {
        return new AdminUserSummaryResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getMobile(),
                user.getStatus(),
                user.getKycStatus(),
                user.isEmailVerified(),
                user.getCreatedAt());
    }
}
