package finadvisor.dto.security;

import finadvisor.validator.StrongPassword;
import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(
        @NotBlank(message = "Reset token is required")
        String token,

        @NotBlank(message = "New password is required")
        @StrongPassword
        String newPassword
) {
}
