package com.netflix.watchspaces.controller;

import com.netflix.watchspaces.domain.dto.request.CreateWatchSpaceRequest;
import com.netflix.watchspaces.domain.dto.response.WatchSpaceResponse;
import com.netflix.watchspaces.security.UserPrincipal;
import com.netflix.watchspaces.service.WatchSpaceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/watch-spaces")
public class WatchSpaceController {

    private final WatchSpaceService watchSpaceService;

    public WatchSpaceController(WatchSpaceService watchSpaceService) {
        this.watchSpaceService = watchSpaceService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<WatchSpaceResponse>> createWatchSpace(
            @Valid @RequestBody CreateWatchSpaceRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }
        return watchSpaceService.createWatchSpace(request, principal.getId())
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @GetMapping
    public Flux<WatchSpaceResponse> getActivePublicSpaces(
            @AuthenticationPrincipal UserPrincipal principal) {
        Long currentUserId = principal != null ? principal.getId() : null;
        return watchSpaceService.getActivePublicSpaces(currentUserId);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<WatchSpaceResponse>> getWatchSpaceById(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long currentUserId = principal != null ? principal.getId() : null;
        return watchSpaceService.getWatchSpaceById(id, currentUserId)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/join-by-code")
    public Mono<ResponseEntity<WatchSpaceResponse>> joinByInviteCode(
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }
        String inviteCode = payload.get("inviteCode");
        if (inviteCode == null || inviteCode.isBlank()) {
            return Mono.just(ResponseEntity.badRequest().build());
        }
        return watchSpaceService.joinByInviteCode(inviteCode, principal.getId())
                .map(ResponseEntity::ok);
    }
}
