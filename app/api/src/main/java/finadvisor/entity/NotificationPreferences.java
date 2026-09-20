package finadvisor.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/** Stored as JSONB on the users table via {@code notification_preferences}. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class NotificationPreferences {

    @Builder.Default
    private boolean email = true;
    @Builder.Default
    private boolean sms = false;
    @Builder.Default
    private boolean push = true;
    @Builder.Default
    private boolean inApp = true;
    @Builder.Default
    private boolean marketing = false;
    @Builder.Default
    private boolean investmentAlerts = true;
    @Builder.Default
    private boolean goalReminders = true;
    @Builder.Default
    private boolean marketUpdates = true;
    @Builder.Default
    private boolean securityAlerts = true;
    @Builder.Default
    private boolean weeklyReports = false;
    @Builder.Default
    private boolean monthlyReports = true;

    public static NotificationPreferences defaults() {
        return NotificationPreferences.builder().build();
    }
}
