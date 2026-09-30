package com.netflix.watchspaces.controller;

import com.netflix.watchspaces.domain.dto.request.InteractionRequest;
import com.netflix.watchspaces.domain.dto.response.InteractionResponse;
import com.netflix.watchspaces.security.UserPrincipal;
import com.netflix.watchspaces.service.InteractionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v1/interactions")
public class InteractionController {

    private final InteractionService interactionService;

    public InteractionController(InteractionService interactionService) {
        this.interactionService = interactionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public Mono<InteractionResponse> recordInteraction(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody InteractionRequest request) {
        if (principal == null) {
            return Mono.error(new org.springframework.security.access.AccessDeniedException("Authentication required"));
        }
        return interactionService.recordInteraction(principal.getId(), request);
    }

    @GetMapping("/history")
    public Mono<List<InteractionResponse>> getWatchHistory(
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return Mono.error(new org.springframework.security.access.AccessDeniedException("Authentication required"));
        }
        return interactionService.getUserWatchHistory(principal.getId());
    }
}
