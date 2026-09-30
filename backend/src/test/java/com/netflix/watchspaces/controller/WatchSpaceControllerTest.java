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
}
