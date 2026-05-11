package com.voting.userservice.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private final String secret = Base64.getEncoder()
            .encodeToString("a-very-strong-and-secure-secret-key-for-testing-purposes-only".getBytes());
    private final long expirationMs = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(secret, expirationMs);
    }

    @Test
    void generateToken_ShouldReturnValidToken() {
        String username = "testuser";
        UUID userId = UUID.randomUUID();
        String role = "ROLE_VOTER";

        String token = jwtService.generateToken(username, userId, role);

        assertNotNull(token);
        assertEquals(username, jwtService.extractUsername(token));
        assertEquals(userId, jwtService.extractUserId(token));
        assertEquals(role, jwtService.extractRole(token));
    }

    @Test
    void isTokenValid_ShouldReturnTrue_WhenTokenIsValid() {
        String username = "testuser";
        UUID userId = UUID.randomUUID();
        String role = "ROLE_VOTER";

        String token = jwtService.generateToken(username, userId, role);

        assertTrue(jwtService.isTokenValid(token, username));
    }

    @Test
    void isTokenValid_ShouldReturnFalse_WhenUsernameDoesNotMatch() {
        String username = "testuser";
        UUID userId = UUID.randomUUID();
        String role = "ROLE_VOTER";

        String token = jwtService.generateToken(username, userId, role);

        assertFalse(jwtService.isTokenValid(token, "wronguser"));
    }

    @Test
    void isTokenValid_ShouldReturnFalse_WhenTokenIsExpired() {
        // Create a JwtService with 0 expiration for this test
        JwtService shortLivedJwtService = new JwtService(secret, -1000); // Expired 1 second ago

        String username = "testuser";
        UUID userId = UUID.randomUUID();
        String role = "ROLE_VOTER";

        String token = shortLivedJwtService.generateToken(username, userId, role);

        assertFalse(shortLivedJwtService.isTokenValid(token, username));
    }

    @Test
    void isTokenValid_ShouldReturnFalse_WhenTokenIsMalformed() {
        assertFalse(jwtService.isTokenValid("malformed.token.here", "testuser"));
    }
}
