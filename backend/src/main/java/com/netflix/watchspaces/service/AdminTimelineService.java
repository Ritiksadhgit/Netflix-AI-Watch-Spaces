package com.netflix.watchspaces.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.watchspaces.domain.dto.request.TimelineEventRequest;
import com.netflix.watchspaces.domain.dto.request.TimelineImportRequest;
import com.netflix.watchspaces.domain.dto.response.TimelineImportResultResponse;
import com.netflix.watchspaces.domain.dto.response.TimelineResponse;
import com.netflix.watchspaces.domain.entity.TimelineEvent;
import com.netflix.watchspaces.domain.entity.Title;
import com.netflix.watchspaces.domain.enums.EventType;
import com.netflix.watchspaces.repository.TimelineEventRepository;
import com.netflix.watchspaces.repository.TitleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdminTimelineService {

    private static final Logger log = LoggerFactory.getLogger(AdminTimelineService.class);

    private final TimelineEventRepository timelineEventRepository;
    private final TitleRepository titleRepository;
    private final ObjectMapper objectMapper;

    public AdminTimelineService(
            TimelineEventRepository timelineEventRepository,
            TitleRepository titleRepository,
            ObjectMapper objectMapper) {
        this.timelineEventRepository = timelineEventRepository;
        this.titleRepository = titleRepository;
        this.objectMapper = objectMapper;
    }

    public Mono<List<TimelineResponse>> getTimelineForTitle(Long titleId) {
        return Mono.fromCallable(() -> {
            Title title = getTitleOrThrow(titleId);
            List<TimelineEvent> events = timelineEventRepository.findByTitleIdOrderByTsSecondsAsc(title.getId());
            return events.stream().map(TimelineResponse::fromEntity).collect(Collectors.toList());
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public Mono<TimelineResponse> createMarker(Long titleId, TimelineEventRequest request) {
        return Mono.fromCallable(() -> {
            Title title = getTitleOrThrow(titleId);
            validateEventRequest(request, title, 0);

            EventType type = parseEventTypeOrThrow(request.getEventType(), 0);

            TimelineEvent event = TimelineEvent.builder()
                    .title(title)
                    .tsSeconds(request.getTsSeconds())
                    .eventType(type)
                    .eventTitle(request.getTitle().trim())
                    .payloadJson(request.getPayloadJson().trim())
                    .build();

            TimelineEvent saved = timelineEventRepository.save(event);
            log.info("Created timeline marker {} ('{}') at {}s for title {}", saved.getId(), saved.getEventTitle(), saved.getTsSeconds(), titleId);
            return TimelineResponse.fromEntity(saved);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public Mono<TimelineResponse> updateMarker(Long titleId, Long eventId, TimelineEventRequest request) {
        return Mono.fromCallable(() -> {
            Title title = getTitleOrThrow(titleId);
            validateEventRequest(request, title, 0);

            TimelineEvent event = timelineEventRepository.findById(eventId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Timeline event not found: " + eventId));

            if (!event.getTitle().getId().equals(titleId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event does not belong to title " + titleId);
            }

            EventType type = parseEventTypeOrThrow(request.getEventType(), 0);

            event.setTsSeconds(request.getTsSeconds());
            event.setEventType(type);
            event.setEventTitle(request.getTitle().trim());
            event.setPayloadJson(request.getPayloadJson().trim());

            TimelineEvent saved = timelineEventRepository.save(event);
            log.info("Updated timeline marker {} for title {}", saved.getId(), titleId);
            return TimelineResponse.fromEntity(saved);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public Mono<Void> deleteMarker(Long titleId, Long eventId) {
        return Mono.fromRunnable(() -> {
            TimelineEvent event = timelineEventRepository.findById(eventId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Timeline event not found: " + eventId));

            if (!event.getTitle().getId().equals(titleId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event does not belong to title " + titleId);
            }

            timelineEventRepository.delete(event);
            log.info("Deleted timeline marker {} from title {}", eventId, titleId);
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Transactional
    public Mono<TimelineImportResultResponse> importTimeline(Long titleId, TimelineImportRequest request) {
        return Mono.fromCallable(() -> {
            Title title = getTitleOrThrow(titleId);

            if (request.getEvents() == null || request.getEvents().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Timeline import payload cannot be empty");
            }

            List<TimelineEvent> validatedEntities = new ArrayList<>();

            // 1. Strict Atomic Validation Phase (Fails entire batch if any event is invalid)
            for (int i = 0; i < request.getEvents().size(); i++) {
                TimelineEventRequest item = request.getEvents().get(i);
                validateEventRequest(item, title, i);
                EventType type = parseEventTypeOrThrow(item.getEventType(), i);

                TimelineEvent entity = TimelineEvent.builder()
                        .title(title)
                        .tsSeconds(item.getTsSeconds())
                        .eventType(type)
                        .eventTitle(item.getTitle().trim())
                        .payloadJson(item.getPayloadJson().trim())
                        .build();

                validatedEntities.add(entity);
            }

            // 2. Atomic Persistence Phase
            if (request.isReplaceExisting()) {
                List<TimelineEvent> existing = timelineEventRepository.findByTitleIdOrderByTsSecondsAsc(titleId);
                timelineEventRepository.deleteAll(existing);
                log.info("Cleared {} existing timeline events for title {} before import", existing.size(), titleId);
            }

            List<TimelineEvent> saved = timelineEventRepository.saveAll(validatedEntities);
            log.info("Successfully imported {} timeline events for title {}", saved.size(), titleId);

            return new TimelineImportResultResponse(
                    titleId,
                    saved.size(),
                    request.isReplaceExisting(),
                    "SUCCESS",
                    "Atomically imported " + saved.size() + " timeline markers"
            );
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private Title getTitleOrThrow(Long titleId) {
        return titleRepository.findById(titleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Title not found: " + titleId));
    }

    private void validateEventRequest(TimelineEventRequest req, Title title, int index) {
        if (req.getTsSeconds() == null || req.getTsSeconds() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Event #" + (index + 1) + ": tsSeconds must be non-negative (received: " + req.getTsSeconds() + ")"
            );
        }

        if (title.getDurationSeconds() != null && req.getTsSeconds() > title.getDurationSeconds()) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Event #" + (index + 1) + ": tsSeconds (" + req.getTsSeconds() + "s) exceeds title duration (" + title.getDurationSeconds() + "s)"
            );
        }

        if (req.getTitle() == null || req.getTitle().trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Event #" + (index + 1) + ": title cannot be blank"
            );
        }

        if (req.getPayloadJson() == null || req.getPayloadJson().trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Event #" + (index + 1) + ": payloadJson cannot be blank"
            );
        }

        // Validate JSON syntax
        try {
            objectMapper.readTree(req.getPayloadJson().trim());
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Event #" + (index + 1) + ": payloadJson is malformed JSON syntax: " + e.getMessage()
            );
        }
    }

    private EventType parseEventTypeOrThrow(String eventTypeStr, int index) {
        if (eventTypeStr == null || eventTypeStr.trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Event #" + (index + 1) + ": eventType is required"
            );
        }

        try {
            return EventType.valueOf(eventTypeStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Event #" + (index + 1) + ": invalid eventType '" + eventTypeStr + "'. Allowed values: [SCENE, TRIVIA, CHARACTER, GLOSSARY, VARIATION]"
            );
        }
    }
}
