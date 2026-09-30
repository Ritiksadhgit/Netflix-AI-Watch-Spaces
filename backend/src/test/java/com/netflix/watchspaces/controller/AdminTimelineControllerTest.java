package com.netflix.watchspaces.controller;

import com.netflix.watchspaces.domain.dto.request.TimelineEventRequest;
import com.netflix.watchspaces.domain.dto.response.TimelineResponse;
import com.netflix.watchspaces.security.UserPrincipal;
import com.netflix.watchspaces.service.AdminTimelineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AdminTimelineControllerTest {

    @Mock
    private AdminTimelineService adminTimelineService;

    @InjectMocks
    private AdminTimelineController adminTimelineController;

    private UserPrincipal adminPrincipal;
    private UserPrincipal viewerPrincipal;

    @BeforeEach
    void setUp() {
        adminPrincipal = UserPrincipal.builder()
                .id(1L)
                .email("admin@netflixspaces.com")
                .role(com.netflix.watchspaces.domain.enums.RoleType.ADMIN)
                .displayName("Chief Admin")
                .build();

        viewerPrincipal = UserPrincipal.builder()
                .id(2L)
                .email("viewer@netflixspaces.com")
                .role(com.netflix.watchspaces.domain.enums.RoleType.VIEWER)
                .displayName("Alex Viewer")
                .build();
    }

    @Test
    void testAdminAccessAllowed() {
        TimelineResponse resp = new TimelineResponse(10L, 1L, 30, "SCENE", "Bridge", "{}");
        when(adminTimelineService.getTimelineForTitle(1L)).thenReturn(Mono.just(List.of(resp)));

        StepVerifier.create(adminTimelineController.getTimeline(1L, adminPrincipal))
                .expectNextMatches(list -> list.size() == 1 && list.get(0).getId().equals(10L))
                .verifyComplete();
    }

    @Test
    void testNonAdminAccessForbiddenWith403() {
        assertThrows(AccessDeniedException.class, () -> {
            adminTimelineController.getTimeline(1L, viewerPrincipal);
        });
    }

    @Test
    void testUnauthenticatedAccessForbidden() {
        assertThrows(AccessDeniedException.class, () -> {
            adminTimelineController.getTimeline(1L, null);
        });
    }

    @Test
    void testCreateMarkerAdminAllowed() {
        TimelineEventRequest req = new TimelineEventRequest(50, "SCENE", "Scene Title", "{}");
        TimelineResponse resp = new TimelineResponse(12L, 1L, 50, "SCENE", "Scene Title", "{}");

        when(adminTimelineService.createMarker(eq(1L), any(TimelineEventRequest.class))).thenReturn(Mono.just(resp));

        StepVerifier.create(adminTimelineController.createMarker(1L, req, adminPrincipal))
                .expectNextMatches(r -> r.getId().equals(12L))
                .verifyComplete();
    }
}
