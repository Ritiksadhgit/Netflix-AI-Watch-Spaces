package com.netflix.watchspaces.controller;

import com.netflix.watchspaces.domain.dto.request.LoginRequest;
import com.netflix.watchspaces.domain.dto.request.RefreshTokenRequest;
import com.netflix.watchspaces.domain.dto.request.RegisterRequest;
import com.netflix.watchspaces.domain.dto.response.AuthResponse;
import com.netflix.watchspaces.domain.dto.response.UserResponse;
import com.netflix.watchspaces.security.UserPrincipal;
import com.netflix.watchspaces.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/refresh")
    public Mono<ResponseEntity<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refreshToken(request)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<ResponseEntity<Void>> logout(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return Mono.just(ResponseEntity.noContent().build());
        }
        return authService.logout(principal.getId())
                .thenReturn(ResponseEntity.noContent().build());
    }

    @GetMapping("/me")
    public Mono<ResponseEntity<UserResponse>> getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }
        return authService.getUserProfile(principal.getId())
                .map(ResponseEntity::ok);
    }
}
