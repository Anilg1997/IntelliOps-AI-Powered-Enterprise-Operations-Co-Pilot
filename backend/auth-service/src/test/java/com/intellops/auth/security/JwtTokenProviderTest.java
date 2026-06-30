package com.intellops.auth.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(tokenProvider, "jwtSecret",
                "test-secret-key-which-is-at-least-256-bits-long-for-hs256-algorithm");
        ReflectionTestUtils.setField(tokenProvider, "jwtExpiration", 3600000L);
        ReflectionTestUtils.setField(tokenProvider, "refreshExpiration", 86400000L);
        ReflectionTestUtils.setField(tokenProvider, "issuer", "test");
    }

    @Test
    void generateToken_and_validate_shouldWork() {
        String token = tokenProvider.generateToken("test@example.com");
        assertThat(token).isNotBlank();
        assertThat(tokenProvider.validateToken(token)).isTrue();
        assertThat(tokenProvider.getEmailFromToken(token)).isEqualTo("test@example.com");
    }

    @Test
    void generateToken_fromAuthentication_shouldWork() {
        User principal = new User("user@example.com", "password", List.of());
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        String token = tokenProvider.generateToken(auth);
        assertThat(token).isNotBlank();
        assertThat(tokenProvider.getEmailFromToken(token)).isEqualTo("user@example.com");
    }

    @Test
    void generateRefreshToken_shouldBeValid() {
        String refresh = tokenProvider.generateRefreshToken("test@example.com");
        assertThat(refresh).isNotBlank();
        assertThat(tokenProvider.validateToken(refresh)).isTrue();
        assertThat(tokenProvider.isRefreshToken(refresh)).isTrue();
    }

    @Test
    void validateToken_withInvalidToken_shouldReturnFalse() {
        assertThat(tokenProvider.validateToken("invalid-token")).isFalse();
    }

    @Test
    void getJwtExpiration_shouldReturnValue() {
        assertThat(tokenProvider.getJwtExpiration()).isEqualTo(3600000L);
    }
}
