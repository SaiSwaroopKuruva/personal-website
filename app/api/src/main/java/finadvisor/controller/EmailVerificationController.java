package finadvisor.controller;

import finadvisor.dto.verification.VerifyEmailRequest;
import finadvisor.service.EmailVerificationService;
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
@RequestMapping("/api/email")
@RequiredArgsConstructor
@Tag(name = "Email Verification", description = "Send, resend and confirm email verification tokens")
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    @PostMapping("/send-verification")
    @Operation(summary = "Send a verification email to the current user")
    public ResponseEntity<Void> sendVerification(Authentication authentication) {
        emailVerificationService.sendVerification(authentication.getName());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify an email address using a token")
    public ResponseEntity<Void> verify(@Valid @RequestBody VerifyEmailRequest request) {
        emailVerificationService.verify(request.token());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/resend")
    @Operation(summary = "Resend the verification email, invalidating any previous token")
    public ResponseEntity<Void> resend(Authentication authentication) {
        emailVerificationService.resend(authentication.getName());
        return ResponseEntity.accepted().build();
    }
}
