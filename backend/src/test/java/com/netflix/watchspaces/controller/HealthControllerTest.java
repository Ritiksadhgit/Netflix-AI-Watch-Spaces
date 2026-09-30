package com.netflix.watchspaces.controller;

import com.netflix.watchspaces.config.SecurityConfig;
import com.netflix.watchspaces.security.JwtAuthenticationManager;
import com.netflix.watchspaces.security.SecurityContextRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = HealthController.class)
@Import(SecurityConfig.class)
class HealthControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private JwtAuthenticationManager jwtAuthenticationManager;

    @MockBean
    private SecurityContextRepository securityContextRepository;

    @BeforeEach
    void setUp() {
        when(securityContextRepository.load(any())).thenReturn(Mono.empty());
    }

    @Test
    @DisplayName("GET /api/v1/health - Should return UP status without authentication")
    void testGetHealthEndpoint() {
        webTestClient.get()
                .uri("/api/v1/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("UP")
                .jsonPath("$.service").isEqualTo("netflix-ai-watch-spaces-backend")
                .jsonPath("$.version").isEqualTo("1.0.0");
    }
}
