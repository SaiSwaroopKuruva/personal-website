package finadvisor.service.impl;

import finadvisor.dto.security.ChangePasswordRequest;
import finadvisor.dto.security.ForgotPasswordRequest;
import finadvisor.dto.security.ResetPasswordRequest;
import finadvisor.entity.PasswordHistoryEntry;
import finadvisor.entity.PasswordResetToken;
import finadvisor.entity.User;
import finadvisor.exception.InvalidCurrentPasswordException;
import finadvisor.exception.InvalidTokenException;
import finadvisor.exception.PasswordReusedException;
import finadvisor.notification.EmailSender;
import finadvisor.repository.PasswordHistoryRepository;
import finadvisor.repository.PasswordResetTokenRepository;
import finadvisor.repository.RefreshTokenRepository;
import finadvisor.repository.UserRepository;
import finadvisor.security.RequestMetadata;
import finadvisor.security.RequestMetadataProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private PasswordHistoryRepository passwordHistoryRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmailSender emailSender;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private RequestMetadataProvider requestMetadataProvider;

    private PasswordServiceImpl passwordService;

    @BeforeEach
    void setUp() {
        passwordService = new PasswordServiceImpl(userRepository, passwordResetTokenRepository,
                passwordHistoryRepository, refreshTokenRepository, passwordEncoder, emailSender,
                eventPublisher, requestMetadataProvider);
        lenient().when(requestMetadataProvider.current()).thenReturn(new RequestMetadata("127.0.0.1", "test-agent"));
    }

    private User sampleUser() {
        return User.builder().id(UUID.randomUUID()).email("asha.rao@example.com").password("current-hash").build();
    }

    @Test
    void forgotPassword_shouldSilentlyDoNothingWhenEmailUnknown() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        passwordService.forgotPassword(new ForgotPasswordRequest("missing@example.com"));

        verify(passwordResetTokenRepository, never()).save(any());
    }

    @Test
    void forgotPassword_shouldIssueTokenAndSendEmailWhenUserExists() {
        User user = sampleUser();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        passwordService.forgotPassword(new ForgotPasswordRequest(user.getEmail()));

        verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
        verify(emailSender).send(org.mockito.ArgumentMatchers.eq(user.getEmail()), any(), any());
    }

    @Test
    void resetPassword_shouldThrowForExpiredToken() {
        User user = sampleUser();
        PasswordResetToken token = PasswordResetToken.builder().id(UUID.randomUUID()).user(user).token("expired")
                .expiresAt(Instant.now().minusSeconds(60)).build();
        when(passwordResetTokenRepository.findByToken("expired")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> passwordService.resetPassword(new ResetPasswordRequest("expired", "NewStrongPass1234!")))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void resetPassword_shouldRejectReuseOfCurrentPassword() {
        User user = sampleUser();
        PasswordResetToken token = PasswordResetToken.builder().id(UUID.randomUUID()).user(user).token("valid")
                .expiresAt(Instant.now().plusSeconds(3600)).build();
        when(passwordResetTokenRepository.findByToken("valid")).thenReturn(Optional.of(token));
        when(passwordEncoder.matches("SamePass1234!", user.getPassword())).thenReturn(true);

        assertThatThrownBy(() -> passwordService.resetPassword(new ResetPasswordRequest("valid", "SamePass1234!")))
                .isInstanceOf(PasswordReusedException.class);
    }

    @Test
    void resetPassword_shouldRejectReuseOfHistoricalPassword() {
        User user = sampleUser();
        PasswordResetToken token = PasswordResetToken.builder().id(UUID.randomUUID()).user(user).token("valid")
                .expiresAt(Instant.now().plusSeconds(3600)).build();
        PasswordHistoryEntry historyEntry = PasswordHistoryEntry.builder().passwordHash("old-hash").build();

        when(passwordResetTokenRepository.findByToken("valid")).thenReturn(Optional.of(token));
        when(passwordEncoder.matches("OldPass1234!", user.getPassword())).thenReturn(false);
        when(passwordHistoryRepository.findTop5ByUser_IdOrderByCreatedAtDesc(user.getId()))
                .thenReturn(List.of(historyEntry));
        when(passwordEncoder.matches("OldPass1234!", "old-hash")).thenReturn(true);

        assertThatThrownBy(() -> passwordService.resetPassword(new ResetPasswordRequest("valid", "OldPass1234!")))
                .isInstanceOf(PasswordReusedException.class);
    }

    @Test
    void resetPassword_shouldUpdatePasswordAndRevokeSessions() {
        User user = sampleUser();
        PasswordResetToken token = PasswordResetToken.builder().id(UUID.randomUUID()).user(user).token("valid")
                .expiresAt(Instant.now().plusSeconds(3600)).build();

        when(passwordResetTokenRepository.findByToken("valid")).thenReturn(Optional.of(token));
        when(passwordEncoder.matches("NewStrongPass1234!", user.getPassword())).thenReturn(false);
        when(passwordHistoryRepository.findTop5ByUser_IdOrderByCreatedAtDesc(user.getId())).thenReturn(List.of());
        when(passwordEncoder.encode("NewStrongPass1234!")).thenReturn("new-hash");

        passwordService.resetPassword(new ResetPasswordRequest("valid", "NewStrongPass1234!"));

        assertThat(user.getPassword()).isEqualTo("new-hash");
        verify(refreshTokenRepository).deleteByUserId(user.getId());
        verify(passwordResetTokenRepository).deleteByUser_Id(user.getId());
    }

    @Test
    void changePassword_shouldThrowWhenCurrentPasswordIncorrect() {
        User user = sampleUser();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-current", user.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> passwordService.changePassword(user.getEmail(),
                new ChangePasswordRequest("wrong-current", "NewStrongPass1234!")))
                .isInstanceOf(InvalidCurrentPasswordException.class);
    }

    @Test
    void changePassword_shouldUpdatePasswordWhenValid() {
        User user = sampleUser();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("current-hash", user.getPassword())).thenReturn(true);
        when(passwordEncoder.matches("NewStrongPass1234!", user.getPassword())).thenReturn(false);
        when(passwordHistoryRepository.findTop5ByUser_IdOrderByCreatedAtDesc(user.getId())).thenReturn(List.of());
        when(passwordEncoder.encode("NewStrongPass1234!")).thenReturn("new-hash");

        passwordService.changePassword(user.getEmail(), new ChangePasswordRequest("current-hash", "NewStrongPass1234!"));

        assertThat(user.getPassword()).isEqualTo("new-hash");
        verify(passwordHistoryRepository).save(any(PasswordHistoryEntry.class));
    }
}
