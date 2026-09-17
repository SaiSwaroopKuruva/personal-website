package finadvisor.dto.admin;

import jakarta.validation.constraints.NotNull;
import finadvisor.entity.UserStatus;

public record UpdateUserStatusRequest(
        @NotNull(message = "Status is required")
        UserStatus status
) {
}
