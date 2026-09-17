package finadvisor.service;

import finadvisor.dto.notification.NotificationPreferencesResponse;
import finadvisor.dto.notification.UpdateNotificationPreferencesRequest;

public interface NotificationPreferenceService {
    NotificationPreferencesResponse getPreferences(String email);

    NotificationPreferencesResponse updatePreferences(String email, UpdateNotificationPreferencesRequest request);
}
