package com.financial.advisor.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;
    // 256-bit secret key required for HMAC-SHA256
    private final String SECRET = "my-super-secret-key-which-must-be-at-least-32-bytes-long";
    private final long EXPIRATION_MS = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils(SECRET, EXPIRATION_MS);
    }

    @Test
    void shouldGenerateAndValidateTokenSuccessfully() {
        String userId = "user-123";
        String role = "ROLE_USER";

        String token = jwtUtils.generateToken(userId, role);
        assertNotNull(token);

        Claims claims = jwtUtils.validateAndExtractClaims(token);
        assertEquals(userId, claims.getSubject());
        assertEquals(role, claims.get("role", String.class));
    }

    @Test
    void shouldExtractUserIdAndRoleDirectly() {
        String token = jwtUtils.generateToken("admin-999", "ROLE_ADMIN");

        assertEquals("admin-999", jwtUtils.extractUserId(token));
        assertEquals("ROLE_ADMIN", jwtUtils.extractRole(token));
    }

    @Test
    void shouldThrowExceptionWhenTokenIsTampered() {
        String token = jwtUtils.generateToken("user-123", "ROLE_USER");
        String tamperedToken = token + "tampered";

        assertThrows(SignatureException.class, () -> jwtUtils.validateAndExtractClaims(tamperedToken));
    }

    @Test
    void shouldThrowExceptionWhenTokenIsExpired() throws InterruptedException {
        // Create a JwtUtils instance with a 1-millisecond expiration
        JwtUtils fastExpiringJwtUtils = new JwtUtils(SECRET, 1);
        String token = fastExpiringJwtUtils.generateToken("user-123", "ROLE_USER");

        // Wait for token to expire
        Thread.sleep(10);

        assertThrows(ExpiredJwtException.class, () -> fastExpiringJwtUtils.validateAndExtractClaims(token));
    }
}