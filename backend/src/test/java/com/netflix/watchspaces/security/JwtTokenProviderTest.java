package com.netflix.watchspaces.security;

import com.netflix.watchspaces.domain.entity.User;
import com.netflix.watchspaces.domain.enums.RoleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;
    private final String secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private final long expirationMs = 900000; // 15 mins

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider(secret, expirationMs);
    }

    @Test
    @DisplayName("Should generate valid JWT access token and extract correct claims")
    void testGenerateAndValidateToken() {
        User user = User.builder()
                .id(42L)
                .email("test@example.com")
                .displayName("Test User")
                .role(RoleType.HOST)
                .build();

        String token = tokenProvider.generateAccessToken(user);

        assertNotNull(token);
        assertTrue(tokenProvider.validateToken(token));
        assertEquals(42L, tokenProvider.getUserIdFromToken(token));
        assertEquals("test@example.com", tokenProvider.getEmailFromToken(token));
        assertEquals("Test User", tokenProvider.getDisplayNameFromToken(token));
        assertEquals(RoleType.HOST, tokenProvider.getRoleFromToken(token));
    }

    @Test
    @DisplayName("Should reject malformed or tampered token")
    void testRejectInvalidToken() {
        assertFalse(tokenProvider.validateToken("invalid.token.string"));
        assertFalse(tokenProvider.validateToken(""));
        assertFalse(tokenProvider.validateToken(null));
    }
}
