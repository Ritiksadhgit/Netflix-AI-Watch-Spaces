package com.netflix.watchspaces.controller;

import com.netflix.watchspaces.domain.dto.request.LoginRequest;
import com.netflix.watchspaces.domain.dto.request.RegisterRequest;
import com.netflix.watchspaces.domain.dto.response.AuthResponse;
import com.netflix.watchspaces.domain.dto.response.UserResponse;
import com.netflix.watchspaces.domain.enums.RoleType;
import com.netflix.watchspaces.security.JwtAuthenticationManager;
import com.netflix.watchspaces.security.SecurityContextRepository;
import com.netflix.watchspaces.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

@org.springframework.context.annotation.Import(com.netflix.watchspaces.config.SecurityConfig.class)
@WebFluxTest(controllers = {AuthController.class, HealthController.class})
class AuthControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtAuthenticationManager jwtAuthenticationManager;

    @MockBean
    private SecurityContextRepository securityContextRepository;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        when(securityContextRepository.load(any())).thenReturn(Mono.empty());
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Should return 201 and AuthResponse on valid input")
    void testRegisterEndpointSuccess() {
        RegisterRequest request = RegisterRequest.builder()
                .email("test@example.com")
                .password("SecurePass123!")
                .displayName("Alex")
                .role(RoleType.VIEWER)
                .build();

        AuthResponse mockResponse = AuthResponse.builder()
                .accessToken("mock.jwt.token")
                .refreshToken("mock-refresh-uuid")
                .tokenType("Bearer")
                .expiresIn(900L)
                .user(UserResponse.builder()
                        .id(1L)
                        .email("test@example.com")
                        .displayName("Alex")
                        .role(RoleType.VIEWER)
                        .build())
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(Mono.just(mockResponse));

        webTestClient.mutateWith(csrf())
                .post()
                .uri("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.accessToken").isEqualTo("mock.jwt.token")
                .jsonPath("$.refreshToken").isEqualTo("mock-refresh-uuid")
                .jsonPath("$.user.email").isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Should return 400 Bad Request if email is invalid")
    void testRegisterInvalidEmail() {
        RegisterRequest request = RegisterRequest.builder()
                .email("not-an-email")
                .password("Password123!")
                .displayName("Alex")
                .build();

        webTestClient.mutateWith(csrf())
                .post()
                .uri("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.code").isEqualTo("ERR_VALIDATION_FAILED")
                .jsonPath("$.validationErrors.email").isNotEmpty();
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Should return 200 and AuthResponse on valid login")
    void testLoginEndpointSuccess() {
        LoginRequest request = LoginRequest.builder()
                .email("viewer@netflixspaces.com")
                .password("Password123!")
                .build();

        AuthResponse mockResponse = AuthResponse.builder()
                .accessToken("mock.login.token")
                .refreshToken("refresh-token-123")
                .tokenType("Bearer")
                .expiresIn(900L)
                .user(UserResponse.builder()
                        .id(2L)
                        .email("viewer@netflixspaces.com")
                        .displayName("Viewer")
                        .role(RoleType.VIEWER)
                        .build())
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(Mono.just(mockResponse));

        webTestClient.mutateWith(csrf())
                .post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.accessToken").isEqualTo("mock.login.token")
                .jsonPath("$.user.email").isEqualTo("viewer@netflixspaces.com");
    }

    @Test
    @DisplayName("GET /api/v1/health - Should return 200 and UP status")
    void testHealthEndpoint() {
        webTestClient.get()
                .uri("/api/v1/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("UP")
                .jsonPath("$.service").isEqualTo("netflix-ai-watch-spaces-backend");
    }
}
