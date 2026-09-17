package finadvisor.dto.security;

import java.time.Instant;
import java.util.UUID;

public record DeviceResponse(
        UUID id,
        String deviceName,
        String browser,
        String ipAddress,
        Instant lastLogin,
        boolean current
) {
}
