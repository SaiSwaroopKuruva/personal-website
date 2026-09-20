package finadvisor.mapper;

import finadvisor.dto.notification.NotificationPreferencesResponse;
import finadvisor.entity.NotificationPreferences;
import org.springframework.stereotype.Component;

@Component
public class NotificationPreferencesMapper {

    public NotificationPreferencesResponse toResponse(NotificationPreferences preferences) {
        return new NotificationPreferencesResponse(
                preferences.isEmail(),
                preferences.isSms(),
                preferences.isPush(),
                preferences.isInApp(),
                preferences.isMarketing(),
                preferences.isInvestmentAlerts(),
                preferences.isGoalReminders(),
                preferences.isMarketUpdates(),
                preferences.isSecurityAlerts(),
                preferences.isWeeklyReports(),
                preferences.isMonthlyReports());
    }
}
