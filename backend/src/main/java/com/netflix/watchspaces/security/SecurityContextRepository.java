package com.netflix.watchspaces.security;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class SecurityContextRepository implements ServerSecurityContextRepository {

    private final JwtAuthenticationManager authenticationManager;

    public SecurityContextRepository(JwtAuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        return Mono.empty();
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String token = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else {
            // Also check query param 'token' (vital for WebSocket connection handshakes)
            token = exchange.getRequest().getQueryParams().getFirst("token");
        }

        if (token != null && !token.isBlank()) {
            Authentication auth = new UsernamePasswordAuthenticationToken(token, token);
            return authenticationManager.authenticate(auth)
                    .map(SecurityContextImpl::new);
        }

        return Mono.empty();
    }
}
