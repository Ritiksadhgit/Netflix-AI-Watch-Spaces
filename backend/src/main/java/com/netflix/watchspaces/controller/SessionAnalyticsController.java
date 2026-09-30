package com.netflix.watchspaces.controller;

import com.netflix.watchspaces.domain.dto.response.SessionAnalyticsResponse;
import com.netflix.watchspaces.service.SessionAnalyticsService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/analytics")
public class SessionAnalyticsController {

    private final SessionAnalyticsService sessionAnalyticsService;

    public SessionAnalyticsController(SessionAnalyticsService sessionAnalyticsService) {
        this.sessionAnalyticsService = sessionAnalyticsService;
    }

    @GetMapping("/session/{watchSpaceId}")
    public Mono<SessionAnalyticsResponse> getSessionAnalytics(@PathVariable String watchSpaceId) {
        return sessionAnalyticsService.getSessionAnalytics(watchSpaceId);
    }

    @PostMapping("/session/{watchSpaceId}/reconcile")
    public Mono<SessionAnalyticsResponse> reconcileSessionAnalytics(@PathVariable String watchSpaceId) {
        return sessionAnalyticsService.reconcileAndSaveSessionAnalytics(watchSpaceId);
    }
}
