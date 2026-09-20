package finadvisor.service.impl;

import finadvisor.dto.notification.NotificationPreferencesResponse;
import finadvisor.dto.notification.UpdateNotificationPreferencesRequest;
import finadvisor.entity.NotificationPreferences;
import finadvisor.entity.User;
import finadvisor.events.AuditEvent;
import finadvisor.exception.UserNotFoundException;
import finadvisor.mapper.NotificationPreferencesMapper;
import finadvisor.repository.UserRepository;
import finadvisor.security.RequestMetadataProvider;
import finadvisor.service.NotificationPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationPreferenceServiceImpl implements NotificationPreferenceService {

    private final UserRepository userRepository;
    private final NotificationPreferencesMapper mapper;
    private final ApplicationEventPublisher eventPublisher;
    private final RequestMetadataProvider requestMetadataProvider;

    @Override
    @Transactional(readOnly = true)
    public NotificationPreferencesResponse getPreferences(String email) {
        return mapper.toResponse(findUser(email).getNotificationPreferences());
    }

    @Override
    public NotificationPreferencesResponse updatePreferences(String email, UpdateNotificationPreferencesRequest request) {
        User user = findUser(email);
        NotificationPreferences preferences = NotificationPreferences.builder()
                .email(request.email())
                .sms(request.sms())
                .push(request.push())
                .inApp(request.inApp())
                .marketing(request.marketing())
                .investmentAlerts(request.investmentAlerts())
                .goalReminders(request.goalReminders())
                .marketUpdates(request.marketUpdates())
                .securityAlerts(request.securityAlerts())
                .weeklyReports(request.weeklyReports())
                .monthlyReports(request.monthlyReports())
                .build();
        user.setNotificationPreferences(preferences);
        User saved = userRepository.save(user);
        eventPublisher.publishEvent(new AuditEvent(user.getId(), "NOTIFICATION_PREFERENCES_UPDATED",
                "Notification preferences updated", requestMetadataProvider.current().ipAddress()));
        return mapper.toResponse(saved.getNotificationPreferences());
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
}
