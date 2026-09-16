package com.example.helloworld.service.impl;

import com.example.helloworld.config.JwtProperties;
import com.example.helloworld.entity.RiskProfile;
import com.example.helloworld.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceImplTest {

    private static final String TEST_SECRET = "test-secret-key-that-is-at-least-256-bits-long-for-hs256!!";

    private JwtServiceImpl jwtService;
    private User user;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(TEST_SECRET);
        properties.setAccessTokenExpirationMs(900_000);
        properties.setRefreshTokenExpirationMs(604_800_000);
        jwtService = new JwtServiceImpl(properties);

        user = User.builder()
                .id(UUID.randomUUID())
                .firstName("Asha")
                .lastName("Rao")
                .email("asha.rao@example.com")
                .mobile("9876543210")
                .password("hashed")
                .riskProfile(RiskProfile.MODERATE)
                .build();
    }

    @Test
    void generateAccessToken_shouldContainSubjectEmail() {
        String token = jwtService.generateAccessToken(user);

        assertThat(jwtService.extractEmail(token)).isEqualTo(user.getEmail());
    }

    @Test
    void isTokenValid_shouldReturnTrueForMatchingEmail() {
        String token = jwtService.generateAccessToken(user);

        assertThat(jwtService.isTokenValid(token, user.getEmail())).isTrue();
    }

    @Test
    void isTokenValid_shouldReturnFalseForDifferentEmail() {
        String token = jwtService.generateAccessToken(user);

        assertThat(jwtService.isTokenValid(token, "someone-else@example.com")).isFalse();
    }

    @Test
    void isTokenValid_shouldReturnFalseForMalformedToken() {
        assertThat(jwtService.isTokenValid("not-a-real-token", user.getEmail())).isFalse();
    }

    @Test
    void getAccessTokenExpirationSeconds_shouldConvertMillisToSeconds() {
        assertThat(jwtService.getAccessTokenExpirationSeconds()).isEqualTo(900);
    }
}
