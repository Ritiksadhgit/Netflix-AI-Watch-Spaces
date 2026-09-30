package com.netflix.watchspaces.service;

import com.netflix.watchspaces.domain.dto.request.LoginRequest;
import com.netflix.watchspaces.domain.dto.request.RegisterRequest;
import com.netflix.watchspaces.domain.dto.response.AuthResponse;
import com.netflix.watchspaces.domain.entity.User;
import com.netflix.watchspaces.domain.enums.RoleType;
import com.netflix.watchspaces.repository.RefreshTokenRepository;
import com.netflix.watchspaces.repository.UserRepository;
import com.netflix.watchspaces.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import reactor.test.StepVerifier;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private PasswordEncoder passwordEncoder;
    private JwtTokenProvider jwtTokenProvider;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        jwtTokenProvider = new JwtTokenProvider(
                "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970",
                900000L
        );
        authService = new AuthService(
                userRepository,
                refreshTokenRepository,
                passwordEncoder,
                jwtTokenProvider,
                604800000L
        );
    }

    @Test
    @DisplayName("Should register new user and hash password with BCrypt")
    void testRegisterSuccess() {
        RegisterRequest request = RegisterRequest.builder()
                .email("newuser@example.com")
                .password("PlainTextPassword123")
                .displayName("New User")
                .role(RoleType.VIEWER)
                .build();

        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);

        User savedUser = User.builder()
                .id(10L)
                .email("newuser@example.com")
                .displayName("New User")
                .passwordHash(passwordEncoder.encode("PlainTextPassword123"))
                .role(RoleType.VIEWER)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        StepVerifier.create(authService.register(request))
                .assertNext(response -> {
                    assertNotNull(response);
                    assertNotNull(response.getAccessToken());
                    assertTrue(jwtTokenProvider.validateToken(response.getAccessToken()));
                    assertEquals("newuser@example.com", response.getUser().getEmail());
                    assertEquals("New User", response.getUser().getDisplayName());
                })
                .verifyComplete();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertTrue(passwordEncoder.matches("PlainTextPassword123", userCaptor.getValue().getPasswordHash()));
    }

    @Test
    @DisplayName("Should reject registration if email is already taken")
    void testRegisterDuplicateEmail() {
        RegisterRequest request = RegisterRequest.builder()
                .email("existing@example.com")
                .password("Password123")
                .displayName("Existing")
                .build();

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        StepVerifier.create(authService.register(request))
                .expectErrorMatches(throwable -> throwable instanceof ResponseStatusException
                        && ((ResponseStatusException) throwable).getStatusCode().value() == 409)
                .verify();

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should log in with valid credentials")
    void testLoginSuccess() {
        LoginRequest request = LoginRequest.builder()
                .email("user@example.com")
                .password("ValidPass123")
                .build();

        User user = User.builder()
                .id(1L)
                .email("user@example.com")
                .displayName("John Doe")
                .passwordHash(passwordEncoder.encode("ValidPass123"))
                .role(RoleType.VIEWER)
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        StepVerifier.create(authService.login(request))
                .assertNext(response -> {
                    assertNotNull(response.getAccessToken());
                    assertEquals("user@example.com", response.getUser().getEmail());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should reject login with invalid password")
    void testLoginInvalidPassword() {
        LoginRequest request = LoginRequest.builder()
                .email("user@example.com")
                .password("WrongPass")
                .build();

        User user = User.builder()
                .id(1L)
                .email("user@example.com")
                .passwordHash(passwordEncoder.encode("CorrectPass123"))
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        StepVerifier.create(authService.login(request))
                .expectErrorMatches(throwable -> throwable instanceof ResponseStatusException
                        && ((ResponseStatusException) throwable).getStatusCode().value() == 401)
                .verify();
    }
}
