package finadvisor.service.impl;

import finadvisor.dto.PageResponse;
import finadvisor.dto.admin.AdminUserSummaryResponse;
import finadvisor.dto.admin.VerificationStatusResponse;
import finadvisor.dto.profile.ProfileResponse;
import finadvisor.entity.RiskProfile;
import finadvisor.entity.User;
import finadvisor.entity.UserStatus;
import finadvisor.events.AuditEvent;
import finadvisor.exception.UserNotFoundException;
import finadvisor.mapper.AdminUserMapper;
import finadvisor.mapper.ProfileMapper;
import finadvisor.repository.RefreshTokenRepository;
import finadvisor.repository.RiskAssessmentResultRepository;
import finadvisor.repository.UserRepository;
import finadvisor.security.RequestMetadataProvider;
import finadvisor.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RiskAssessmentResultRepository riskAssessmentResultRepository;
    private final AdminUserMapper adminUserMapper;
    private final ProfileMapper profileMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final RequestMetadataProvider requestMetadataProvider;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminUserSummaryResponse> getUsers(int page, int size) {
        return PageResponse.of(userRepository.findAll(PageRequest.of(page, size)).map(adminUserMapper::toSummaryResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getUser(UUID userId) {
        return profileMapper.toResponse(findUser(userId));
    }

    @Override
    public ProfileResponse suspendUser(UUID userId, String adminEmail) {
        User user = findUser(userId);
        user.setStatus(UserStatus.SUSPENDED);
        user.setUpdatedBy(adminEmail);
        User saved = userRepository.save(user);
        refreshTokenRepository.deleteByUserId(user.getId());
        publishAudit(user, "USER_SUSPENDED", "Account suspended by admin " + adminEmail);
        return profileMapper.toResponse(saved);
    }

    @Override
    public ProfileResponse activateUser(UUID userId, String adminEmail) {
        User user = findUser(userId);
        user.setStatus(UserStatus.ACTIVE);
        user.setUpdatedBy(adminEmail);
        User saved = userRepository.save(user);
        publishAudit(user, "USER_ACTIVATED", "Account activated by admin " + adminEmail);
        return profileMapper.toResponse(saved);
    }

    @Override
    public void resetRiskProfile(UUID userId, String adminEmail) {
        User user = findUser(userId);
        riskAssessmentResultRepository.deleteByUser_Id(user.getId());
        user.setRiskProfile(RiskProfile.MODERATE);
        userRepository.save(user);
        publishAudit(user, "RISK_PROFILE_RESET", "Risk profile reset by admin " + adminEmail);
    }

    @Override
    @Transactional(readOnly = true)
    public VerificationStatusResponse getVerificationStatus(UUID userId) {
        User user = findUser(userId);
        return new VerificationStatusResponse(user.isEmailVerified(), user.isMobileVerified(), user.getKycStatus().name());
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private void publishAudit(User user, String action, String details) {
        eventPublisher.publishEvent(new AuditEvent(user.getId(), action, details,
                requestMetadataProvider.current().ipAddress()));
    }
}
