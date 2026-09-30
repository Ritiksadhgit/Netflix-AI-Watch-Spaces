package com.netflix.watchspaces.service;

import com.netflix.watchspaces.domain.dto.request.LoginRequest;
import com.netflix.watchspaces.domain.dto.request.RefreshTokenRequest;
import com.netflix.watchspaces.domain.dto.request.RegisterRequest;
import com.netflix.watchspaces.domain.dto.response.AuthResponse;
import com.netflix.watchspaces.domain.dto.response.UserResponse;
import com.netflix.watchspaces.domain.entity.RefreshToken;
import com.netflix.watchspaces.domain.entity.User;
import com.netflix.watchspaces.domain.enums.RoleType;
import com.netflix.watchspaces.repository.RefreshTokenRepository;
import com.netflix.watchspaces.repository.UserRepository;
import com.netflix.watchspaces.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final long refreshTokenExpirationMs;

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            @Value("${app.jwt.refresh-token-expiration-ms:604800000}") long refreshTokenExpirationMs) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    public Mono<AuthResponse> register(RegisterRequest request) {
        return Mono.fromCallable(() -> {
            if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
            }

            RoleType role = request.getRole() != null ? request.getRole() : RoleType.VIEWER;

            User user = User.builder()
                    .email(request.getEmail().trim().toLowerCase())
                    .passwordHash(passwordEncoder.encode(request.getPassword()))
                    .displayName(request.getDisplayName().trim())
                    .role(role)
                    .avatarUrl(request.getAvatarUrl() != null && !request.getAvatarUrl().isBlank()
                            ? request.getAvatarUrl().trim()
                            : "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&q=80")
                    .subtitleLocale("en-US")
                    .build();

            User savedUser = userRepository.save(user);
            return createAuthResponse(savedUser);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<AuthResponse> login(LoginRequest request) {
        return Mono.fromCallable(() -> {
            String email = request.getEmail().trim().toLowerCase();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

            if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
            }

            return createAuthResponse(user);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public Mono<AuthResponse> refreshToken(RefreshTokenRequest request) {
        return Mono.fromCallable(() -> {
            String tokenHash = request.getRefreshToken().trim();
            RefreshToken token = refreshTokenRepository.findByTokenHashAndRevokedFalse(tokenHash)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or revoked refresh token"));

            if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
                token.setRevoked(true);
                refreshTokenRepository.save(token);
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token has expired");
            }

            // Invalidate the old token (token rotation)
            token.setRevoked(true);
            refreshTokenRepository.save(token);

            User user = token.getUser();
            return createAuthResponse(user);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public Mono<Void> logout(Long userId) {
        return Mono.fromRunnable(() -> {
            userRepository.findById(userId).ifPresent(refreshTokenRepository::revokeAllByUser);
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    public Mono<UserResponse> getUserProfile(Long userId) {
        return Mono.fromCallable(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
            return UserResponse.fromEntity(user);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private AuthResponse createAuthResponse(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshTokenHash = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(refreshTokenHash)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpirationMs / 1000))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenHash)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpirationMs() / 1000)
                .user(UserResponse.fromEntity(user))
                .build();
    }
}
