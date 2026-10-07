package com.netflix.watchspaces.controller;

import com.netflix.watchspaces.domain.dto.request.CreateWatchSpaceRequest;
import com.netflix.watchspaces.domain.dto.response.WatchSpaceResponse;
import com.netflix.watchspaces.domain.enums.RoleType;
import com.netflix.watchspaces.security.UserPrincipal;
import com.netflix.watchspaces.service.WatchSpaceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
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

    @PostMapping("/{id}/pin-fact")
    public Mono<ResponseEntity<Map<String, Object>>> pinFact(
            @PathVariable String id,
            @RequestBody Map<String, Object> fact,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }
        boolean isAdmin = principal.getRole() == RoleType.ADMIN;
        return watchSpaceService.pinFact(id, fact, principal.getId(), isAdmin)
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}/pin-fact")
    public Mono<ResponseEntity<Void>> unpinFact(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }
        boolean isAdmin = principal.getRole() == RoleType.ADMIN;
        return watchSpaceService.unpinFact(id, principal.getId(), isAdmin)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    @GetMapping("/{id}/pinned-fact")
    public Mono<ResponseEntity<Map<String, Object>>> getPinnedFact(@PathVariable String id) {
        return watchSpaceService.getPinnedFact(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.ok(Map.of()));
    }

    @PostMapping("/{id}/variant/approve")
    public Mono<ResponseEntity<Map<String, Object>>> approveVariant(
            @PathVariable String id,
            @RequestBody Map<String, Object> variant,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }
        boolean isAdmin = principal.getRole() == RoleType.ADMIN;
        return watchSpaceService.approveVariant(id, variant, principal.getId(), isAdmin)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{id}/variant/approved")
    public Mono<ResponseEntity<Map<String, Object>>> getApprovedVariant(@PathVariable String id) {
        return watchSpaceService.getApprovedVariant(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.ok(Map.of()));
    }

    @GetMapping("/{id}/variants/eligible")
    public Mono<ResponseEntity<List<Map<String, Object>>>> getEligibleVariants(@PathVariable String id) {
        return watchSpaceService.getEligibleVariants(id)
                .map(ResponseEntity::ok);
    }
}
