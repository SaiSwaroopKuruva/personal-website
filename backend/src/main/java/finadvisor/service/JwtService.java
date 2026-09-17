package finadvisor.service;

import finadvisor.entity.User;

public interface JwtService {

    String generateAccessToken(User user);

    String extractEmail(String token);

    boolean isTokenValid(String token, String expectedEmail);

    long getAccessTokenExpirationSeconds();

    long getRefreshTokenExpirationMs();
}
