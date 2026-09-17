package finadvisor.events;

import java.util.UUID;

/** Published whenever a user-initiated action should be recorded in the audit trail. */
public record AuditEvent(UUID userId, String action, String details, String ipAddress) {
}
