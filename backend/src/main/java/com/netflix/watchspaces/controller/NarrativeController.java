package com.netflix.watchspaces.controller;

import com.netflix.watchspaces.domain.dto.response.VariationAppliedResponse;
import com.netflix.watchspaces.domain.dto.response.VariationVoteOpenResponse;
import com.netflix.watchspaces.security.UserPrincipal;
import com.netflix.watchspaces.service.NarrativeService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
public class NarrativeController {

    private final NarrativeService narrativeService;

    public NarrativeController(NarrativeService narrativeService) {
        this.narrativeService = narrativeService;
    }

    @GetMapping("/api/v1/titles/{titleId}/variations")
    public Flux<VariationVoteOpenResponse> getVariationsForTitle(@PathVariable Long titleId) {
        return narrativeService.getVariationsForTitle(titleId);
    }

    @PostMapping("/api/v1/watch-spaces/{watchSpaceId}/variation/vote")
    public Mono<Map<String, Object>> castVote(
            @PathVariable String watchSpaceId,
            @RequestBody Map<String, Object> payload,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal != null ? principal.getId() : 1L;
        Number variationIdNum = (Number) payload.get("variationId");
        Number optionIdNum = (Number) payload.get("optionId");

        Long variationId = variationIdNum != null ? variationIdNum.longValue() : 0L;
        Long optionId = optionIdNum != null ? optionIdNum.longValue() : 0L;

        return narrativeService.castVote(watchSpaceId, userId, variationId, optionId);
    }

    @PostMapping("/api/v1/watch-spaces/{watchSpaceId}/variation/finalize")
    public Mono<VariationAppliedResponse> finalizeVote(
            @PathVariable String watchSpaceId,
            @RequestBody Map<String, Object> payload) {
        Number variationIdNum = (Number) payload.get("variationId");
        Long variationId = variationIdNum != null ? variationIdNum.longValue() : 0L;
        return narrativeService.finalizeVote(watchSpaceId, variationId);
    }
}
