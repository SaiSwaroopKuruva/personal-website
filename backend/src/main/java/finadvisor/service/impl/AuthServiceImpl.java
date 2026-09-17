package finadvisor.service.impl;

import finadvisor.dto.auth.AuthResponse;
import finadvisor.dto.auth.LoginRequest;
import finadvisor.dto.auth.RefreshTokenRequest;
import finadvisor.dto.auth.RegisterRequest;
import finadvisor.dto.auth.UserResponse;
import finadvisor.entity.RefreshToken;
import finadvisor.entity.User;
import finadvisor.exception.EmailAlreadyExistsException;
import finadvisor.exception.InvalidCredentialsException;
import finadvisor.exception.InvalidRefreshTokenException;
import finadvisor.exception.MobileAlreadyExistsException;
import finadvisor.exception.UserNotFoundException;
import finadvisor.repository.RefreshTokenRepository;
import finadvisor.repository.UserRepository;
import finadvisor.service.AuthService;
import finadvisor.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("An account with this email already exists");
        }
        if (userRepository.existsByMobile(request.mobile())) {
            throw new MobileAlreadyExistsException("An account with this mobile number already exists");
        }

        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .mobile(request.mobile())
                .password(passwordEncoder.encode(request.password()))
                .riskProfile(request.riskProfile())
                .build();

        User savedUser = userRepository.save(user);
        return buildAuthResponse(savedUser);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        return buildAuthResponse(user);
    }

    @Override
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken existingToken = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token is invalid or has expired"));

        if (existingToken.isExpired()) {
            refreshTokenRepository.delete(existingToken);
            throw new InvalidRefreshTokenException("Refresh token is invalid or has expired");
        }

        User user = existingToken.getUser();
        refreshTokenRepository.delete(existingToken);
        return buildAuthResponse(user);
    }

    @Override
    public void logout(RefreshTokenRequest request) {
        refreshTokenRepository.deleteByToken(request.refreshToken());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return toUserResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshTokenValue = createRefreshToken(user);
        return AuthResponse.of(accessToken, refreshTokenValue, jwtService.getAccessTokenExpirationSeconds(), toUserResponse(user));
    }

    private String createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(jwtService.getRefreshTokenExpirationMs()))
                .build();
        return refreshTokenRepository.save(refreshToken).getToken();
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getMobile(),
                user.getRiskProfile(),
                user.getCreatedAt());
    }
}
