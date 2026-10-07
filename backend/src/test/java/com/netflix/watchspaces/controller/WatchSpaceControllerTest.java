package com.netflix.watchspaces.controller;

import com.netflix.watchspaces.config.SecurityConfig;
import com.netflix.watchspaces.domain.dto.request.CreateWatchSpaceRequest;
import com.netflix.watchspaces.domain.dto.response.TitleResponse;
import com.netflix.watchspaces.domain.dto.response.UserResponse;
import com.netflix.watchspaces.domain.dto.response.WatchSpaceResponse;
import com.netflix.watchspaces.domain.enums.PlaybackState;
import com.netflix.watchspaces.domain.enums.PrivacyType;
import com.netflix.watchspaces.domain.enums.RoleType;
import com.netflix.watchspaces.domain.enums.WatchSpaceStatus;
import com.netflix.watchspaces.security.JwtAuthenticationManager;
import com.netflix.watchspaces.security.SecurityContextRepository;
import com.netflix.watchspaces.security.UserPrincipal;
import com.netflix.watchspaces.service.WatchSpaceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

@WebFluxTest(controllers = WatchSpaceController.class)
@Import(SecurityConfig.class)
class WatchSpaceControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private WatchSpaceService watchSpaceService;

    @MockBean
    private JwtAuthenticationManager jwtAuthenticationManager;

    @MockBean
    private SecurityContextRepository securityContextRepository;

    private UserPrincipal testUserPrincipal;

    @BeforeEach
    void setUp() {
        testUserPrincipal = UserPrincipal.builder()
                .id(42L)
                .email("host@example.com")
                .displayName("Party Host")
                .role(RoleType.HOST)
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_HOST")))
                .build();

        Authentication auth = new UsernamePasswordAuthenticationToken(testUserPrincipal, "token", testUserPrincipal.getAuthorities());
        when(securityContextRepository.load(any())).thenReturn(Mono.just(new org.springframework.security.core.context.SecurityContextImpl(auth)));
    }

    @Test
    @DisplayName("POST /api/v1/watch-spaces - Should create new Watch Space")
    void testCreateWatchSpaceSuccess() {
        CreateWatchSpaceRequest request = CreateWatchSpaceRequest.builder()
                .titleId(1L)
                .name("Sci-Fi Stream Party")
                .privacy(PrivacyType.PUBLIC)
                .maxParticipants(50)
                .build();

        WatchSpaceResponse mockResponse = WatchSpaceResponse.builder()
                .id("ws_test_88")
                .name("Sci-Fi Stream Party")
                .status(WatchSpaceStatus.LIVE)
                .privacy(PrivacyType.PUBLIC)
                .inviteCode("CODE-1234")
                .playbackState(PlaybackState.PAUSED)
                .playbackPositionSeconds(0.0)
                .participantCount(1)
                .isHost(true)
                .build();

        when(watchSpaceService.createWatchSpace(any(CreateWatchSpaceRequest.class), eq(42L)))
                .thenReturn(Mono.just(mockResponse));

        webTestClient.mutateWith(csrf())
                .post()
                .uri("/api/v1/watch-spaces")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("ws_test_88")
                .jsonPath("$.name").isEqualTo("Sci-Fi Stream Party")
                .jsonPath("$.inviteCode").isEqualTo("CODE-1234");
    }

    @Test
    @DisplayName("POST /api/v1/watch-spaces/join-by-code - Should join room with valid invite code")
    void testJoinByCodeSuccess() {
        WatchSpaceResponse mockResponse = WatchSpaceResponse.builder()
                .id("ws_cyber_2026")
                .name("Cyberpunk Premiere")
                .inviteCode("CYBER-2026")
                .participantCount(3)
                .isHost(false)
                .build();

        when(watchSpaceService.joinByInviteCode(eq("CYBER-2026"), eq(42L)))
                .thenReturn(Mono.just(mockResponse));

        webTestClient.mutateWith(csrf())
                .post()
                .uri("/api/v1/watch-spaces/join-by-code")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("inviteCode", "CYBER-2026"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("ws_cyber_2026")
                .jsonPath("$.inviteCode").isEqualTo("CYBER-2026");
    }

    @Test
    @DisplayName("POST /api/v1/watch-spaces/{id}/pin-fact - Host pins fact")
    void testPinFactSuccess() {
        Map<String, Object> fact = Map.of(
                "answer", "Thom retrofitted his cybernetic arm.",
                "currentScene", "The Bridge"
        );

        when(watchSpaceService.pinFact(eq("ws_123"), any(), eq(42L), eq(false)))
                .thenReturn(Mono.just(fact));

        webTestClient.mutateWith(csrf())
                .post()
                .uri("/api/v1/watch-spaces/ws_123/pin-fact")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(fact)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.answer").isEqualTo("Thom retrofitted his cybernetic arm.");
    }

    @Test
    @DisplayName("POST /api/v1/watch-spaces/{id}/variant/approve - Host approves localized variant")
    void testApproveVariantSuccess() {
        Map<String, Object> variant = Map.of(
                "variantId", "es",
                "label", "Español",
                "type", "SUBTITLE"
        );

        when(watchSpaceService.approveVariant(eq("ws_123"), any(), eq(42L), eq(false)))
                .thenReturn(Mono.just(variant));

        webTestClient.mutateWith(csrf())
                .post()
                .uri("/api/v1/watch-spaces/ws_123/variant/approve")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(variant)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.variantId").isEqualTo("es")
                .jsonPath("$.label").isEqualTo("Español");
    }

    @Test
    @DisplayName("GET /api/v1/watch-spaces/{id}/variants/eligible - Retrieves eligible localized variants")
    void testGetEligibleVariants() {
        java.util.List<Map<String, Object>> variants = java.util.List.of(
                Map.of("id", "en", "label", "English [CC]", "lang", "en", "type", "SUBTITLE"),
                Map.of("id", "es", "label", "Español", "lang", "es", "type", "SUBTITLE")
        );

        when(watchSpaceService.getEligibleVariants(eq("ws_123")))
                .thenReturn(Mono.just(variants));

        webTestClient
                .get()
                .uri("/api/v1/watch-spaces/ws_123/variants/eligible")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].id").isEqualTo("en")
                .jsonPath("$[1].id").isEqualTo("es");
    }
}
