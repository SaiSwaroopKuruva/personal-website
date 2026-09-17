package finadvisor.service;

import finadvisor.dto.PageResponse;
import finadvisor.dto.admin.AdminUserSummaryResponse;
import finadvisor.dto.admin.VerificationStatusResponse;
import finadvisor.dto.profile.ProfileResponse;

import java.util.UUID;

public interface AdminUserService {
    PageResponse<AdminUserSummaryResponse> getUsers(int page, int size);

    ProfileResponse getUser(UUID userId);

    ProfileResponse suspendUser(UUID userId, String adminEmail);

    ProfileResponse activateUser(UUID userId, String adminEmail);

    void resetRiskProfile(UUID userId, String adminEmail);

    VerificationStatusResponse getVerificationStatus(UUID userId);
}
