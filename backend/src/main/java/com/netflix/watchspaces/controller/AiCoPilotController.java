package com.netflix.watchspaces.controller;

import com.netflix.watchspaces.domain.dto.request.AiQuestionRequest;
import com.netflix.watchspaces.domain.dto.response.AiAnswerResponse;
import com.netflix.watchspaces.domain.dto.response.TimelineResponse;
import com.netflix.watchspaces.domain.dto.response.TriviaEventResponse;
import com.netflix.watchspaces.domain.entity.WatchSpace;
import com.netflix.watchspaces.repository.WatchSpaceRepository;
import com.netflix.watchspaces.service.AiGroundingService;
import com.netflix.watchspaces.service.TimelineService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class AiCoPilotController {

    private final AiGroundingService aiGroundingService;
    private final TimelineService timelineService;
    private final WatchSpaceRepository watchSpaceRepository;

    public AiCoPilotController(
            AiGroundingService aiGroundingService,
            TimelineService timelineService,
            WatchSpaceRepository watchSpaceRepository) {
        this.aiGroundingService = aiGroundingService;
        this.timelineService = timelineService;
        this.watchSpaceRepository = watchSpaceRepository;
    }

    @PostMapping("/api/v1/watch-spaces/{watchSpaceId}/ai/ask")
    public Mono<AiAnswerResponse> askInWatchSpace(
            @PathVariable String watchSpaceId,
            @Valid @RequestBody AiQuestionRequest request) {
        return Mono.fromCallable(() -> {
            WatchSpace space = watchSpaceRepository.findById(watchSpaceId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Watch space not found"));

            if (space.getTitle() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No title associated with this watch space");
            }
            return space;
        }).flatMap(space -> {
            Long titleId = space.getTitle().getId();
            String verbosity = space.getAiVerbosity() != null ? space.getAiVerbosity().name() : "NORMAL";
            return aiGroundingService.answerQuestion(
                    titleId,
                    request.getQuestion(),
                    request.getCurrentTimestamp() != null ? request.getCurrentTimestamp() : 0.0,
                    verbosity
            );
        });
    }

    @PostMapping("/api/v1/titles/{titleId}/ai/ask")
    public Mono<AiAnswerResponse> askInTitleCatalog(
            @PathVariable Long titleId,
            @Valid @RequestBody AiQuestionRequest request) {
        return aiGroundingService.answerQuestion(
                titleId,
                request.getQuestion(),
                request.getCurrentTimestamp() != null ? request.getCurrentTimestamp() : 0.0,
                "NORMAL"
        );
    }

    @GetMapping("/api/v1/titles/{titleId}/timeline")
    public Flux<TimelineResponse> getTimelineEvents(@PathVariable Long titleId) {
        return timelineService.getEventsForTitle(titleId);
    }

    @GetMapping("/api/v1/titles/{titleId}/trivia")
    public Flux<TriviaEventResponse> getTriviaEvents(@PathVariable Long titleId) {
        return timelineService.getTriviaForTitle(titleId);
    }
}
