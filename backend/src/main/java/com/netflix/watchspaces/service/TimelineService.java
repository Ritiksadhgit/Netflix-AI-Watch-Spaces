package com.netflix.watchspaces.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.watchspaces.domain.dto.response.TimelineResponse;
import com.netflix.watchspaces.domain.dto.response.TriviaEventResponse;
import com.netflix.watchspaces.domain.entity.TimelineEvent;
import com.netflix.watchspaces.domain.entity.WatchSpace;
import com.netflix.watchspaces.domain.enums.EventType;
import com.netflix.watchspaces.repository.TimelineEventRepository;
import com.netflix.watchspaces.repository.WatchSpaceRepository;
import com.netflix.watchspaces.websocket.WatchSpaceRoomManager;
import com.netflix.watchspaces.websocket.WatchSpaceRoomSession;
import com.netflix.watchspaces.websocket.event.WebSocketEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TimelineService {

    private static final Logger log = LoggerFactory.getLogger(TimelineService.class);

    private final TimelineEventRepository timelineEventRepository;
    private final WatchSpaceRepository watchSpaceRepository;
    private final WatchSpaceRoomManager roomManager;
    private final ObjectMapper objectMapper;

    public TimelineService(
            TimelineEventRepository timelineEventRepository,
            WatchSpaceRepository watchSpaceRepository,
            WatchSpaceRoomManager roomManager,
            ObjectMapper objectMapper) {
        this.timelineEventRepository = timelineEventRepository;
        this.watchSpaceRepository = watchSpaceRepository;
        this.roomManager = roomManager;
        this.objectMapper = objectMapper;
    }

    public Flux<TimelineResponse> getEventsForTitle(Long titleId) {
        return Mono.fromCallable(() -> {
            List<TimelineEvent> events = timelineEventRepository.findByTitleIdOrderByTsSecondsAsc(titleId);
            return events.stream().map(TimelineResponse::fromEntity).collect(Collectors.toList());
        }).subscribeOn(Schedulers.boundedElastic()).flatMapMany(Flux::fromIterable);
    }

    public Flux<TriviaEventResponse> getTriviaForTitle(Long titleId) {
        return Mono.fromCallable(() -> {
            List<TimelineEvent> events = timelineEventRepository.findByTitleIdAndEventType(titleId, EventType.TRIVIA);
            List<TriviaEventResponse> result = new ArrayList<>();
            for (TimelineEvent ev : events) {
                parseTriviaEvent(ev).ifPresent(result::add);
            }
            return result;
        }).subscribeOn(Schedulers.boundedElastic()).flatMapMany(Flux::fromIterable);
    }

    public Mono<Void> checkAndTriggerTrivia(String watchSpaceId, double currentPosition) {
        return Mono.fromRunnable(() -> {
            Optional<WatchSpaceRoomSession> roomOpt = roomManager.getRoom(watchSpaceId);
            if (roomOpt.isEmpty()) return;

            WatchSpaceRoomSession room = roomOpt.get();
            Optional<WatchSpace> spaceOpt = watchSpaceRepository.findById(watchSpaceId);
            if (spaceOpt.isEmpty() || spaceOpt.get().getTitle() == null) return;

            Long titleId = spaceOpt.get().getTitle().getId();
            List<TimelineEvent> triviaEvents = timelineEventRepository.findByTitleIdAndEventType(titleId, EventType.TRIVIA);

            for (TimelineEvent ev : triviaEvents) {
                double diff = currentPosition - ev.getTsSeconds();

                // Trigger window: within [0s, 3s] after passing the marker
                if (diff >= 0 && diff <= 3.0) {
                    if (!room.isTriviaTriggered(ev.getId())) {
                        room.markTriviaTriggered(ev.getId());
                        parseTriviaEvent(ev).ifPresent(trivia -> {
                            log.info("Triggering trivia {} for room {} at pos {}", ev.getId(), watchSpaceId, currentPosition);
                            room.broadcast(WebSocketEnvelope.of("room.ai.trivia", watchSpaceId, trivia));
                        });
                    }
                }
            }
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    private Optional<TriviaEventResponse> parseTriviaEvent(TimelineEvent ev) {
        try {
            JsonNode node = objectMapper.readTree(ev.getPayloadJson());
            String question = node.has("question") ? node.get("question").asText() : ev.getEventTitle();
            List<String> options = new ArrayList<>();
            if (node.has("options") && node.get("options").isArray()) {
                node.get("options").forEach(opt -> options.add(opt.asText()));
            }
            int answerIndex = node.has("answerIndex") ? node.get("answerIndex").asInt() : 0;
            int durationSeconds = node.has("durationSeconds") ? node.get("durationSeconds").asInt() : 15;
            String note = node.has("triviaNote") ? node.get("triviaNote").asText() : "";

            return Optional.of(new TriviaEventResponse(
                    ev.getId(),
                    ev.getTitle() != null ? ev.getTitle().getId() : null,
                    ev.getTsSeconds(),
                    question,
                    options,
                    answerIndex,
                    durationSeconds,
                    note
            ));
        } catch (Exception e) {
            log.error("Failed to parse trivia payload for event {}: {}", ev.getId(), e.getMessage());
            return Optional.empty();
        }
    }
}
