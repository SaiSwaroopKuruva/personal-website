package finadvisor.controller;

import finadvisor.dto.security.ChangePasswordRequest;
import finadvisor.dto.security.ForgotPasswordRequest;
import finadvisor.dto.security.ResetPasswordRequest;
import finadvisor.service.PasswordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/password")
@RequiredArgsConstructor
@Tag(name = "Password Management", description = "Forgot/reset/change password flows")
public class PasswordController {

    private final PasswordService passwordService;

    @PostMapping("/forgot")
    @Operation(summary = "Request a password reset token")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordService.forgotPassword(request);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/reset")
    @Operation(summary = "Reset the password using a reset token")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordService.resetPassword(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/change")
    @Operation(summary = "Change the current user's password")
    public ResponseEntity<Void> changePassword(Authentication authentication,
                                                @Valid @RequestBody ChangePasswordRequest request) {
        passwordService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok().build();
    }
}
