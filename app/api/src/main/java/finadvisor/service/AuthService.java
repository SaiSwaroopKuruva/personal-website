package finadvisor.service;

import finadvisor.dto.auth.AuthResponse;
import finadvisor.dto.auth.LoginRequest;
import finadvisor.dto.auth.RefreshTokenRequest;
import finadvisor.dto.auth.RegisterRequest;
import finadvisor.dto.auth.UserResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(RefreshTokenRequest request);

    void logout(RefreshTokenRequest request);

    UserResponse getCurrentUser(String email);
}
