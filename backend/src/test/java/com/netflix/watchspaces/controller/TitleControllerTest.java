package com.netflix.watchspaces.controller;

import com.netflix.watchspaces.config.SecurityConfig;
import com.netflix.watchspaces.domain.dto.response.TitleResponse;
import com.netflix.watchspaces.security.JwtAuthenticationManager;
import com.netflix.watchspaces.security.SecurityContextRepository;
import com.netflix.watchspaces.service.TitleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = TitleController.class)
@Import(SecurityConfig.class)
class TitleControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private TitleService titleService;

    @MockBean
    private JwtAuthenticationManager jwtAuthenticationManager;

    @MockBean
    private SecurityContextRepository securityContextRepository;

    @BeforeEach
    void setUp() {
        when(securityContextRepository.load(any())).thenReturn(Mono.empty());
    }

    @Test
    @DisplayName("GET /api/v1/titles - Should return list of catalog titles")
    void testGetAllTitles() {
        TitleResponse t1 = TitleResponse.builder()
                .id(1L)
                .name("Tears of Steel")
                .synopsis("Sci-Fi dystopian Amsterdam")
                .durationSeconds(734)
                .videoAssetUrl("https://example.com/video.mp4")
                .genres("Sci-Fi, Action")
                .build();

        when(titleService.getAllTitles()).thenReturn(Flux.just(t1));

        webTestClient.get()
                .uri("/api/v1/titles")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(1)
                .jsonPath("$[0].name").isEqualTo("Tears of Steel");
    }

    @Test
    @DisplayName("GET /api/v1/titles/{id} - Should return single title by ID")
    void testGetTitleById() {
        TitleResponse t1 = TitleResponse.builder()
                .id(2L)
                .name("Sintel")
                .synopsis("Fantasy dragon quest")
                .durationSeconds(888)
                .videoAssetUrl("https://example.com/sintel.mp4")
                .genres("Fantasy, Adventure")
                .build();

        when(titleService.getTitleById(2L)).thenReturn(Mono.just(t1));

        webTestClient.get()
                .uri("/api/v1/titles/2")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(2)
                .jsonPath("$.name").isEqualTo("Sintel");
    }

    @Test
    @DisplayName("Performance Benchmark: Non-AI REST Controller P95 Latency < 200ms (WebTestClient Mock HTTP)")
    void testNonAiApiP95Latency() {
        TitleResponse t1 = TitleResponse.builder()
                .id(1L)
                .name("Tears of Steel")
                .synopsis("Sci-Fi dystopian Amsterdam")
                .durationSeconds(734)
                .videoAssetUrl("https://example.com/video.mp4")
                .genres("Sci-Fi, Action")
                .build();

        when(titleService.getAllTitles()).thenReturn(Flux.just(t1));

        for (int i = 0; i < 5; i++) {
            webTestClient.get().uri("/api/v1/titles").exchange().expectStatus().isOk();
        }

        int iterations = 40;
        java.util.List<Long> latenciesMs = new java.util.ArrayList<>();

        for (int i = 0; i < iterations; i++) {
            long start = System.nanoTime();
            webTestClient.get().uri("/api/v1/titles").exchange().expectStatus().isOk();
            long elapsed = (System.nanoTime() - start) / 1_000_000;
            latenciesMs.add(elapsed);
        }

        java.util.Collections.sort(latenciesMs);
        int p50Idx = (int) Math.ceil(0.50 * latenciesMs.size()) - 1;
        int p95Idx = (int) Math.ceil(0.95 * latenciesMs.size()) - 1;
        long p50 = latenciesMs.get(p50Idx);
        long p95 = latenciesMs.get(p95Idx);

        System.out.println(String.format("[BENCHMARK REPORT] Non-AI REST Controller Latency (WebTestClient GET /api/v1/titles): P50=%dms, P95=%dms (Target: <200ms)", p50, p95));
        org.junit.jupiter.api.Assertions.assertTrue(p95 < 200, "Non-AI REST controller P95 latency must be strictly under 200ms");
    }
}
