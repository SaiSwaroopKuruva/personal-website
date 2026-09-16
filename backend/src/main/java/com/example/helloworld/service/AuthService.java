package com.example.helloworld.service;

import com.example.helloworld.dto.auth.AuthResponse;
import com.example.helloworld.dto.auth.LoginRequest;
import com.example.helloworld.dto.auth.RefreshTokenRequest;
import com.example.helloworld.dto.auth.RegisterRequest;
import com.example.helloworld.dto.auth.UserResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(RefreshTokenRequest request);

    void logout(RefreshTokenRequest request);

    UserResponse getCurrentUser(String email);
}
