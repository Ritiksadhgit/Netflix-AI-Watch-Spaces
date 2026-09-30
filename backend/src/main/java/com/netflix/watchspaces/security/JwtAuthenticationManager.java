package com.netflix.watchspaces.security;

import com.netflix.watchspaces.domain.enums.RoleType;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Component
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtTokenProvider tokenProvider;

    public JwtAuthenticationManager(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String token = authentication.getCredentials().toString();

        if (token == null || !tokenProvider.validateToken(token)) {
            return Mono.empty();
        }

        Long userId = tokenProvider.getUserIdFromToken(token);
        String email = tokenProvider.getEmailFromToken(token);
        String displayName = tokenProvider.getDisplayNameFromToken(token);
        RoleType role = tokenProvider.getRoleFromToken(token);

        UserPrincipal principal = UserPrincipal.builder()
                .id(userId)
                .email(email)
                .displayName(displayName)
                .role(role)
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name())))
                .build();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, token, principal.getAuthorities());

        return Mono.just(auth);
    }
}
