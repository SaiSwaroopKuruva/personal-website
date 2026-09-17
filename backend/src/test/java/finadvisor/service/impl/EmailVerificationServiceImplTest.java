package finadvisor.service.impl;

import finadvisor.entity.EmailVerificationToken;
import finadvisor.entity.User;
import finadvisor.exception.EmailAlreadyVerifiedException;
import finadvisor.exception.InvalidTokenException;
import finadvisor.notification.EmailSender;
import finadvisor.repository.EmailVerificationTokenRepository;
import finadvisor.repository.UserRepository;
import finadvisor.security.RequestMetadata;
import finadvisor.security.RequestMetadataProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private EmailVerificationTokenRepository tokenRepository;
    @Mock
    private EmailSender emailSender;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private RequestMetadataProvider requestMetadataProvider;

    private EmailVerificationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new EmailVerificationServiceImpl(userRepository, tokenRepository, emailSender,
                eventPublisher, requestMetadataProvider);
        lenient().when(requestMetadataProvider.current()).thenReturn(new RequestMetadata("127.0.0.1", "test-agent"));
    }

    private User sampleUser() {
        return User.builder().id(UUID.randomUUID()).email("asha.rao@example.com").emailVerified(false).build();
    }

    @Test
    void sendVerification_shouldThrowWhenAlreadyVerified() {
        User user = sampleUser();
        user.setEmailVerified(true);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.sendVerification(user.getEmail()))
                .isInstanceOf(EmailAlreadyVerifiedException.class);
    }

    @Test
    void sendVerification_shouldIssueTokenAndSendEmail() {
        User user = sampleUser();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        service.sendVerification(user.getEmail());

        verify(tokenRepository).save(any(EmailVerificationToken.class));
        verify(emailSender).send(org.mockito.ArgumentMatchers.eq(user.getEmail()), any(), any());
    }

    @Test
    void verify_shouldThrowForExpiredToken() {
        User user = sampleUser();
        EmailVerificationToken token = EmailVerificationToken.builder().id(UUID.randomUUID()).user(user)
                .token("expired").expiresAt(Instant.now().minusSeconds(60)).build();
        when(tokenRepository.findByToken("expired")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.verify("expired")).isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void verify_shouldMarkUserVerifiedForValidToken() {
        User user = sampleUser();
        EmailVerificationToken token = EmailVerificationToken.builder().id(UUID.randomUUID()).user(user)
                .token("valid").expiresAt(Instant.now().plusSeconds(3600)).build();
        when(tokenRepository.findByToken("valid")).thenReturn(Optional.of(token));

        service.verify("valid");

        assertThat(user.isEmailVerified()).isTrue();
        verify(userRepository).save(user);
        verify(tokenRepository).deleteByUser_Id(user.getId());
    }
}
