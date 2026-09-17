package finadvisor.dto.notification;

import jakarta.validation.constraints.NotNull;

public record UpdateNotificationPreferencesRequest(
        @NotNull(message = "email is required") Boolean email,
        @NotNull(message = "sms is required") Boolean sms,
        @NotNull(message = "push is required") Boolean push,
        @NotNull(message = "inApp is required") Boolean inApp,
        @NotNull(message = "marketing is required") Boolean marketing,
        @NotNull(message = "investmentAlerts is required") Boolean investmentAlerts,
        @NotNull(message = "goalReminders is required") Boolean goalReminders,
        @NotNull(message = "marketUpdates is required") Boolean marketUpdates,
        @NotNull(message = "securityAlerts is required") Boolean securityAlerts,
        @NotNull(message = "weeklyReports is required") Boolean weeklyReports,
        @NotNull(message = "monthlyReports is required") Boolean monthlyReports
) {
}
