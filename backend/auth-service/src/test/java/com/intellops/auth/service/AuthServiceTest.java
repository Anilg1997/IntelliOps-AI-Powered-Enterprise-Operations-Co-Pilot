package com.intellops.auth.service;

import com.intellops.auth.dto.*;
import com.intellops.auth.entity.User;
import com.intellops.auth.repository.UserRepository;
import com.intellops.auth.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(authenticationManager, userRepository, passwordEncoder, tokenProvider);
    }

    @Test
    void register_withNewEmail_shouldReturnAuthResponse() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFirstName("John");
        request.setLastName("Doe");

        User user = User.builder()
                .email("test@example.com")
                .password("encoded-password")
                .firstName("John")
                .lastName("Doe")
                .role("USER")
                .enabled(true)
                .build();

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(tokenProvider.generateToken("test@example.com")).thenReturn("jwt-token");
        when(tokenProvider.generateRefreshToken("test@example.com")).thenReturn("refresh-token");
        when(tokenProvider.getJwtExpiration()).thenReturn(3600000L);

        AuthResponse response = authService.register(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getExpiresIn()).isEqualTo(3600000L);
    }

    @Test
    void register_withExistingEmail_shouldThrow() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email already registered");
    }

    @Test
    void login_withValidCredentials_shouldReturnAuthResponse() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("password");

        Authentication auth = mock(Authentication.class);
        User user = User.builder().email("user@example.com").firstName("Jane").build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(tokenProvider.generateToken(auth)).thenReturn("jwt-token");
        when(tokenProvider.generateRefreshToken("user@example.com")).thenReturn("refresh-token");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(tokenProvider.getJwtExpiration()).thenReturn(3600000L);

        AuthResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
    }

    @Test
    void getCurrentUser_withValidEmail_shouldReturnUserDto() {
        User user = User.builder()
                .email("user@example.com")
                .firstName("John")
                .lastName("Doe")
                .role("USER")
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        UserDto dto = authService.getCurrentUser("user@example.com");

        assertThat(dto.getEmail()).isEqualTo("user@example.com");
        assertThat(dto.getFirstName()).isEqualTo("John");
    }

    @Test
    void getCurrentUser_withInvalidEmail_shouldThrow() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.getCurrentUser("unknown@example.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }
}
