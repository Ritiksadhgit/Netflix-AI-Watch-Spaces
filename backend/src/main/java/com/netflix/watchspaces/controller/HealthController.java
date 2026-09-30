package com.netflix.watchspaces.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    private final Instant startTime = Instant.now();

    @GetMapping
    public Mono<ResponseEntity<Map<String, Object>>> getHealth() {
        return Mono.just(ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "netflix-ai-watch-spaces-backend",
                "version", "1.0.0",
                "startedAt", startTime.toString(),
                "timestamp", Instant.now().toString()
        )));
    }
}
