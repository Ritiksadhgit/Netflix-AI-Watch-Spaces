package com.netflix.watchspaces.config;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter implements WebFilter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String CORRELATION_ID_KEY = "correlationId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String correlationId = request.getHeaders().getFirst(CORRELATION_ID_HEADER);

        if (correlationId == null || correlationId.isBlank()) {
            correlationId = "corr_" + UUID.randomUUID().toString().substring(0, 8);
        }

        final String finalCorrelationId = correlationId;
        exchange.getResponse().getHeaders().add(CORRELATION_ID_HEADER, finalCorrelationId);

        return chain.filter(exchange)
                .contextWrite(context -> context.put(CORRELATION_ID_KEY, finalCorrelationId))
                .doOnEach(signal -> {
                    if (signal.isOnNext() || signal.isOnError() || signal.isOnComplete()) {
                        MDC.put(CORRELATION_ID_KEY, finalCorrelationId);
                    }
                })
                .doFinally(signalType -> MDC.remove(CORRELATION_ID_KEY));
    }
}
