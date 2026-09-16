package com.example.helloworld.dto.auth;

import com.example.helloworld.entity.RiskProfile;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String mobile,
        RiskProfile riskProfile,
        Instant createdAt
) {
}
