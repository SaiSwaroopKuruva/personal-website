package com.example.helloworld.service;

import com.example.helloworld.entity.User;

public interface JwtService {

    String generateAccessToken(User user);

    String extractEmail(String token);

    boolean isTokenValid(String token, String expectedEmail);

    long getAccessTokenExpirationSeconds();

    long getRefreshTokenExpirationMs();
}
