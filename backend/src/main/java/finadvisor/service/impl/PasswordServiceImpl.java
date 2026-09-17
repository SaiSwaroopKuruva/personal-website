package finadvisor.service.impl;

import finadvisor.dto.security.ChangePasswordRequest;
import finadvisor.dto.security.ForgotPasswordRequest;
import finadvisor.dto.security.ResetPasswordRequest;
import finadvisor.entity.PasswordHistoryEntry;
import finadvisor.entity.PasswordResetToken;
import finadvisor.entity.User;
import finadvisor.events.AuditEvent;
import finadvisor.exception.InvalidCurrentPasswordException;
import finadvisor.exception.InvalidTokenException;
import finadvisor.exception.PasswordReusedException;
import finadvisor.exception.UserNotFoundException;
import finadvisor.notification.EmailSender;
import finadvisor.repository.PasswordHistoryRepository;
import finadvisor.repository.PasswordResetTokenRepository;
import finadvisor.repository.RefreshTokenRepository;
import finadvisor.repository.UserRepository;
import finadvisor.security.RequestMetadataProvider;
import finadvisor.service.PasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PasswordServiceImpl implements PasswordService {

    private static final Duration RESET_TOKEN_VALIDITY = Duration.ofHours(1);
    private static final int PASSWORD_HISTORY_LIMIT = 5;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordHistoryRepository passwordHistoryRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailSender emailSender;
    private final ApplicationEventPublisher eventPublisher;
    private final RequestMetadataProvider requestMetadataProvider;

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.email()).ifPresent(user -> {
            passwordResetTokenRepository.deleteByUser_Id(user.getId());
            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .user(user)
                    .token(token)
                    .expiresAt(Instant.now().plus(RESET_TOKEN_VALIDITY))
                    .build();
            passwordResetTokenRepository.save(resetToken);
            emailSender.send(user.getEmail(), "Reset your password",
                    "Use the following token to reset your password: " + token
                            + ". This token expires in 1 hour.");
            publishAudit(user, "PASSWORD_RESET_REQUESTED", "Password reset requested");
        });
        // Intentionally does not reveal whether the email exists to prevent account enumeration.
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.token())
                .orElseThrow(() -> new InvalidTokenException("Reset token is invalid or has expired"));

        if (resetToken.isExpired()) {
            passwordResetTokenRepository.delete(resetToken);
            throw new InvalidTokenException("Reset token is invalid or has expired");
        }

        User user = resetToken.getUser();
        assertPasswordNotReused(user, request.newPassword());
        applyNewPassword(user, request.newPassword());
        passwordResetTokenRepository.deleteByUser_Id(user.getId());
        refreshTokenRepository.deleteByUserId(user.getId());
        publishAudit(user, "PASSWORD_RESET", "Password reset via forgot-password flow");
    }

    @Override
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new InvalidCurrentPasswordException("Current password is incorrect");
        }
        assertPasswordNotReused(user, request.newPassword());
        applyNewPassword(user, request.newPassword());
        publishAudit(user, "PASSWORD_CHANGED", "Password changed by the user");
    }

    private void assertPasswordNotReused(User user, String newPassword) {
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new PasswordReusedException("You cannot reuse your current password");
        }
        List<PasswordHistoryEntry> history =
                passwordHistoryRepository.findTop5ByUser_IdOrderByCreatedAtDesc(user.getId());
        boolean reused = history.stream()
                .anyMatch(entry -> passwordEncoder.matches(newPassword, entry.getPasswordHash()));
        if (reused) {
            throw new PasswordReusedException("You cannot reuse any of your last " + PASSWORD_HISTORY_LIMIT + " passwords");
        }
    }

    private void applyNewPassword(User user, String newPassword) {
        passwordHistoryRepository.save(PasswordHistoryEntry.builder()
                .user(user)
                .passwordHash(user.getPassword())
                .build());
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    private void publishAudit(User user, String action, String details) {
        eventPublisher.publishEvent(new AuditEvent(user.getId(), action, details,
                requestMetadataProvider.current().ipAddress()));
    }
}
