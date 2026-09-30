package com.netflix.watchspaces.controller;

import com.netflix.watchspaces.domain.dto.response.TitleResponse;
import com.netflix.watchspaces.service.TitleService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/titles")
public class TitleController {

    private final TitleService titleService;

    public TitleController(TitleService titleService) {
        this.titleService = titleService;
    }

    @GetMapping
    public Flux<TitleResponse> getAllTitles() {
        return titleService.getAllTitles();
    }

    @GetMapping("/{titleId}")
    public Mono<TitleResponse> getTitleById(@PathVariable Long titleId) {
        return titleService.getTitleById(titleId);
    }
}
