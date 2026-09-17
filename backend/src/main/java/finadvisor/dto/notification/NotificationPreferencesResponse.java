package finadvisor.dto.notification;

public record NotificationPreferencesResponse(
        boolean email,
        boolean sms,
        boolean push,
        boolean inApp,
        boolean marketing,
        boolean investmentAlerts,
        boolean goalReminders,
        boolean marketUpdates,
        boolean securityAlerts,
        boolean weeklyReports,
        boolean monthlyReports
) {
}
