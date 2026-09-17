package finadvisor.service.impl;

import finadvisor.entity.EmailVerificationToken;
import finadvisor.entity.User;
import finadvisor.events.AuditEvent;
import finadvisor.exception.EmailAlreadyVerifiedException;
import finadvisor.exception.InvalidTokenException;
import finadvisor.exception.UserNotFoundException;
import finadvisor.notification.EmailSender;
import finadvisor.repository.EmailVerificationTokenRepository;
import finadvisor.repository.UserRepository;
import finadvisor.security.RequestMetadataProvider;
import finadvisor.service.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private static final Duration TOKEN_VALIDITY = Duration.ofHours(24);

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final EmailSender emailSender;
    private final ApplicationEventPublisher eventPublisher;
    private final RequestMetadataProvider requestMetadataProvider;

    @Override
    public void sendVerification(String email) {
        User user = findUser(email);
        if (user.isEmailVerified()) {
            throw new EmailAlreadyVerifiedException("This email address is already verified");
        }
        issueToken(user);
    }

    @Override
    public void verify(String token) {
        EmailVerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Verification token is invalid or has expired"));

        if (verificationToken.isExpired()) {
            tokenRepository.delete(verificationToken);
            throw new InvalidTokenException("Verification token is invalid or has expired");
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);
        tokenRepository.deleteByUser_Id(user.getId());
        publishAudit(user, "EMAIL_VERIFIED", "Email address verified");
    }

    @Override
    public void resend(String email) {
        User user = findUser(email);
        if (user.isEmailVerified()) {
            throw new EmailAlreadyVerifiedException("This email address is already verified");
        }
        tokenRepository.deleteByUser_Id(user.getId());
        issueToken(user);
    }

    private void issueToken(User user) {
        String token = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .user(user)
                .token(token)
                .expiresAt(Instant.now().plus(TOKEN_VALIDITY))
                .build();
        tokenRepository.save(verificationToken);
        emailSender.send(user.getEmail(), "Verify your email address",
                "Use the following token to verify your email: " + token
                        + ". This token expires in 24 hours.");
        publishAudit(user, "EMAIL_VERIFICATION_SENT", "Verification email sent");
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private void publishAudit(User user, String action, String details) {
        eventPublisher.publishEvent(new AuditEvent(user.getId(), action, details,
                requestMetadataProvider.current().ipAddress()));
    }
}
