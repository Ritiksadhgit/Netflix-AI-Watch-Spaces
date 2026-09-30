package com.netflix.watchspaces.controller;

import com.netflix.watchspaces.domain.dto.request.TimelineEventRequest;
import com.netflix.watchspaces.domain.dto.request.TimelineImportRequest;
import com.netflix.watchspaces.domain.dto.response.TimelineImportResultResponse;
import com.netflix.watchspaces.domain.dto.response.TimelineResponse;
import com.netflix.watchspaces.security.UserPrincipal;
import com.netflix.watchspaces.service.AdminTimelineService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/titles/{titleId}/timeline")
public class AdminTimelineController {

    private final AdminTimelineService adminTimelineService;

    public AdminTimelineController(AdminTimelineService adminTimelineService) {
        this.adminTimelineService = adminTimelineService;
    }

    @GetMapping
    public Mono<List<TimelineResponse>> getTimeline(
            @PathVariable Long titleId,
            @AuthenticationPrincipal UserPrincipal principal) {
        verifyAdminAccess(principal);
        return adminTimelineService.getTimelineForTitle(titleId);
    }

    @PostMapping("/events")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<TimelineResponse> createMarker(
            @PathVariable Long titleId,
            @Valid @RequestBody TimelineEventRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        verifyAdminAccess(principal);
        return adminTimelineService.createMarker(titleId, request);
    }

    @PutMapping("/events/{eventId}")
    public Mono<TimelineResponse> updateMarker(
            @PathVariable Long titleId,
            @PathVariable Long eventId,
            @Valid @RequestBody TimelineEventRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        verifyAdminAccess(principal);
        return adminTimelineService.updateMarker(titleId, eventId, request);
    }

    @DeleteMapping("/events/{eventId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteMarker(
            @PathVariable Long titleId,
            @PathVariable Long eventId,
            @AuthenticationPrincipal UserPrincipal principal) {
        verifyAdminAccess(principal);
        return adminTimelineService.deleteMarker(titleId, eventId);
    }

    @PostMapping("/import")
    public Mono<TimelineImportResultResponse> importTimeline(
            @PathVariable Long titleId,
            @Valid @RequestBody TimelineImportRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        verifyAdminAccess(principal);
        return adminTimelineService.importTimeline(titleId, request);
    }

    @GetMapping("/export")
    public Mono<List<TimelineResponse>> exportTimeline(
            @PathVariable Long titleId,
            @AuthenticationPrincipal UserPrincipal principal) {
        verifyAdminAccess(principal);
        return adminTimelineService.getTimelineForTitle(titleId);
    }

    private void verifyAdminAccess(UserPrincipal principal) {
        if (principal == null || principal.getRole() != com.netflix.watchspaces.domain.enums.RoleType.ADMIN) {
            throw new AccessDeniedException("Access denied: ADMIN authority is strictly required for timeline studio operations");
        }
    }
}
