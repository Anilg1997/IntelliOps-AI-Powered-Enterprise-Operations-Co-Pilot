package com.intellops.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellops.auth.dto.*;
import com.intellops.auth.security.JwtTokenProvider;
import com.intellops.auth.service.AuthService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = AuthController.class,
            excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
@org.springframework.test.context.TestPropertySource(properties = {
        "jwt.secret=test-secret-key-for-unit-tests-minimum-32-characters",
        "jwt.expiration=3600000",
        "jwt.refresh-expiration=86400000",
        "jwt.issuer=test"
})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    void register_shouldReturnAuthResponse() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("new@example.com");
        request.setPassword("Pass123!");
        request.setFirstName("John");
        request.setLastName("Doe");

        UserDto userDto = new UserDto();
        userDto.setEmail("new@example.com");
        userDto.setFirstName("John");
        userDto.setLastName("Doe");

        AuthResponse authResponse = AuthResponse.of("token", "refresh", 3600000L, userDto);

        when(authService.register(any())).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("token"))
                .andExpect(jsonPath("$.user.email").value("new@example.com"));
    }

    @Test
    void login_shouldReturnAuthResponse() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");

        UserDto userDto = new UserDto();
        userDto.setEmail("test@example.com");
        AuthResponse authResponse = AuthResponse.of("jwt-token", "refresh", 3600000L, userDto);
        when(authService.login(any())).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void me_shouldReturnUserProfile() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}
