package com.netflix.watchspaces.controller;

import com.netflix.watchspaces.domain.dto.response.RecommendationResponse;
import com.netflix.watchspaces.security.UserPrincipal;
import com.netflix.watchspaces.service.RecommendationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping
    public Mono<List<RecommendationResponse>> getRecommendations(
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal != null ? principal.getId() : null;
        return recommendationService.getPersonalizedRecommendations(userId);
    }

    @GetMapping("/categories")
    public Mono<Map<String, List<RecommendationResponse>>> getCategorizedRecommendations(
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal != null ? principal.getId() : null;
        return recommendationService.getCategorizedRecommendations(userId);
    }
}
