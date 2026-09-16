package com.example.helloworld.service.impl;

import com.example.helloworld.dto.auth.LoginRequest;
import com.example.helloworld.dto.auth.RefreshTokenRequest;
import com.example.helloworld.dto.auth.RegisterRequest;
import com.example.helloworld.entity.RefreshToken;
import com.example.helloworld.entity.RiskProfile;
import com.example.helloworld.entity.User;
import com.example.helloworld.exception.EmailAlreadyExistsException;
import com.example.helloworld.exception.InvalidCredentialsException;
import com.example.helloworld.exception.InvalidRefreshTokenException;
import com.example.helloworld.exception.MobileAlreadyExistsException;
import com.example.helloworld.repository.RefreshTokenRepository;
import com.example.helloworld.repository.UserRepository;
import com.example.helloworld.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(userRepository, refreshTokenRepository, passwordEncoder, jwtService);
    }

    private User sampleUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .firstName("Asha")
                .lastName("Rao")
                .email("asha.rao@example.com")
                .mobile("9876543210")
                .password("hashed-password")
                .riskProfile(RiskProfile.MODERATE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    void register_shouldCreateUserAndReturnTokens() {
        RegisterRequest request = new RegisterRequest("Asha", "Rao", "asha.rao@example.com",
                "9876543210", "StrongPass1", RiskProfile.MODERATE);
        User savedUser = sampleUser();

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.existsByMobile(request.mobile())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateAccessToken(savedUser)).thenReturn("access-token");
        when(jwtService.getAccessTokenExpirationSeconds()).thenReturn(900L);
        when(jwtService.getRefreshTokenExpirationMs()).thenReturn(604_800_000L);
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = authService.register(request);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.user().email()).isEqualTo("asha.rao@example.com");
        assertThat(response.refreshToken()).isNotBlank();
    }

    @Test
    void register_shouldThrowWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("Asha", "Rao", "asha.rao@example.com",
                "9876543210", "StrongPass1", RiskProfile.MODERATE);
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_shouldThrowWhenMobileAlreadyExists() {
        RegisterRequest request = new RegisterRequest("Asha", "Rao", "asha.rao@example.com",
                "9876543210", "StrongPass1", RiskProfile.MODERATE);
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.existsByMobile(request.mobile())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(MobileAlreadyExistsException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_shouldReturnTokensForValidCredentials() {
        User user = sampleUser();
        LoginRequest request = new LoginRequest(user.getEmail(), "plainPassword");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("plainPassword", user.getPassword())).thenReturn(true);
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(jwtService.getAccessTokenExpirationSeconds()).thenReturn(900L);
        when(jwtService.getRefreshTokenExpirationMs()).thenReturn(604_800_000L);
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo("access-token");
    }

    @Test
    void login_shouldThrowForUnknownEmail() {
        LoginRequest request = new LoginRequest("missing@example.com", "password");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_shouldThrowForWrongPassword() {
        User user = sampleUser();
        LoginRequest request = new LoginRequest(user.getEmail(), "wrongPassword");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", user.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void refresh_shouldRotateTokenAndReturnNewPair() {
        User user = sampleUser();
        RefreshToken existingToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .user(user)
                .token("old-refresh-token")
                .expiryDate(Instant.now().plusSeconds(3600))
                .build();
        RefreshTokenRequest request = new RefreshTokenRequest("old-refresh-token");

        when(refreshTokenRepository.findByToken("old-refresh-token")).thenReturn(Optional.of(existingToken));
        when(jwtService.generateAccessToken(user)).thenReturn("new-access-token");
        when(jwtService.getAccessTokenExpirationSeconds()).thenReturn(900L);
        when(jwtService.getRefreshTokenExpirationMs()).thenReturn(604_800_000L);
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = authService.refresh(request);

        assertThat(response.accessToken()).isEqualTo("new-access-token");
        verify(refreshTokenRepository).delete(existingToken);
    }

    @Test
    void refresh_shouldThrowForExpiredToken() {
        User user = sampleUser();
        RefreshToken expiredToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .user(user)
                .token("expired-token")
                .expiryDate(Instant.now().minusSeconds(3600))
                .build();
        RefreshTokenRequest request = new RefreshTokenRequest("expired-token");

        when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(InvalidRefreshTokenException.class);
        verify(refreshTokenRepository).delete(expiredToken);
    }

    @Test
    void refresh_shouldThrowForUnknownToken() {
        RefreshTokenRequest request = new RefreshTokenRequest("unknown-token");
        when(refreshTokenRepository.findByToken("unknown-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void logout_shouldDeleteRefreshToken() {
        RefreshTokenRequest request = new RefreshTokenRequest("token-to-remove");

        authService.logout(request);

        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
        verify(refreshTokenRepository).deleteByToken(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue()).isEqualTo("token-to-remove");
    }

    @Test
    void getCurrentUser_shouldReturnUserResponse() {
        User user = sampleUser();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        var response = authService.getCurrentUser(user.getEmail());

        assertThat(response.email()).isEqualTo(user.getEmail());
    }
}
