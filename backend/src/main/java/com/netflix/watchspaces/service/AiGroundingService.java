package com.netflix.watchspaces.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.watchspaces.domain.dto.response.AiAnswerResponse;
import com.netflix.watchspaces.domain.dto.response.AiSourceCitation;
import com.netflix.watchspaces.domain.entity.TimelineEvent;
import com.netflix.watchspaces.domain.enums.EventType;
import com.netflix.watchspaces.repository.TimelineEventRepository;
import com.netflix.watchspaces.repository.TitleRepository;
import com.netflix.watchspaces.util.InputSanitizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.*;

@Service
public class AiGroundingService {

    private static final Logger log = LoggerFactory.getLogger(AiGroundingService.class);

    private final TimelineEventRepository timelineEventRepository;
    private final TitleRepository titleRepository;
    private final ObjectMapper objectMapper;

    public AiGroundingService(
            TimelineEventRepository timelineEventRepository,
            TitleRepository titleRepository,
            ObjectMapper objectMapper) {
        this.timelineEventRepository = timelineEventRepository;
        this.titleRepository = titleRepository;
        this.objectMapper = objectMapper;
    }

    public Mono<AiAnswerResponse> answerQuestion(Long titleId, String rawQuestion, double timestamp, String verbosity) {
        return Mono.fromCallable(() -> {
            String cleanQuestion = InputSanitizer.sanitizeAiQuestion(rawQuestion);
            if (cleanQuestion.isBlank()) {
                cleanQuestion = "What is happening in this scene?";
            }

            int curSec = (int) Math.floor(timestamp);
            int fromSec = Math.max(0, curSec - 90);
            int toSec = curSec + 30;

            // 1. Retrieve authored events within window [ts - 90s, ts + 30s]
            List<TimelineEvent> windowEvents = timelineEventRepository.findByTitleIdAndTsRange(titleId, fromSec, toSec);
            if (windowEvents.isEmpty()) {
                // Fallback to all events for title if window empty
                windowEvents = timelineEventRepository.findByTitleIdOrderByTsSecondsAsc(titleId);
            }

            // 2. Identify active scene
            TimelineEvent activeScene = windowEvents.stream()
                    .filter(e -> e.getEventType() == EventType.SCENE && e.getTsSeconds() <= curSec)
                    .reduce((first, second) -> second)
                    .orElse(windowEvents.stream().filter(e -> e.getEventType() == EventType.SCENE).findFirst().orElse(null));

            String currentSceneName = activeScene != null ? activeScene.getEventTitle() : "Current Sequence";

            // 3. Match question keywords against retrieved timeline events
            List<MatchedEvent> matched = findMatches(cleanQuestion, windowEvents);

            String answer;
            List<AiSourceCitation> citations = new ArrayList<>();

            if (!matched.isEmpty()) {
                // Grounded answer from matched timeline events
                StringBuilder sb = new StringBuilder();
                for (MatchedEvent me : matched) {
                    TimelineEvent ev = me.event;
                    citations.add(new AiSourceCitation(
                            ev.getId(),
                            ev.getTsSeconds(),
                            ev.getEventType().name(),
                            ev.getEventTitle(),
                            me.snippet
                    ));
                    sb.append(me.narrative).append(" ");
                }
                answer = sb.toString().trim();
            } else {
                // Strict anti-hallucination fallback
                String topic = extractTopic(cleanQuestion);
                String sceneSummary = "";
                if (activeScene != null) {
                    try {
                        JsonNode node = objectMapper.readTree(activeScene.getPayloadJson());
                        sceneSummary = node.has("description") ? node.get("description").asText() : activeScene.getEventTitle();
                    } catch (Exception e) {
                        sceneSummary = activeScene.getEventTitle();
                    }
                    citations.add(new AiSourceCitation(
                            activeScene.getId(),
                            activeScene.getTsSeconds(),
                            activeScene.getEventType().name(),
                            activeScene.getEventTitle(),
                            sceneSummary
                    ));
                }

                answer = "Based on the authored timeline for this title, details regarding \"" + topic +
                        "\" are not documented. Here is what is happening in the current scene (" + currentSceneName + "): " +
                        (sceneSummary.isBlank() ? "The sequence continues along the authoritative narrative timeline." : sceneSummary);
            }

            return new AiAnswerResponse(answer, currentSceneName, timestamp, citations);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private List<MatchedEvent> findMatches(String question, List<TimelineEvent> events) {
        String lowerQ = question.toLowerCase();
        List<MatchedEvent> list = new ArrayList<>();

        for (TimelineEvent ev : events) {
            String titleLower = ev.getEventTitle().toLowerCase();
            String payload = ev.getPayloadJson() != null ? ev.getPayloadJson() : "{}";
            String payloadLower = payload.toLowerCase();

            boolean matches = false;
            String snippet = "";
            String narrative = "";

            try {
                JsonNode node = objectMapper.readTree(payload);

                if (ev.getEventType() == EventType.CHARACTER) {
                    String charName = node.has("name") ? node.get("name").asText() : ev.getEventTitle();
                    String role = node.has("role") ? node.get("role").asText() : "";
                    String desc = node.has("description") ? node.get("description").asText() : "";

                    if (lowerQ.contains(charName.toLowerCase()) || lowerQ.contains("who is") || lowerQ.contains("character")) {
                        matches = true;
                        snippet = charName + ": " + role + " - " + desc;
                        narrative = charName + " is " + role + ". " + desc;
                    }
                } else if (ev.getEventType() == EventType.GLOSSARY) {
                    String term = node.has("term") ? node.get("term").asText() : ev.getEventTitle();
                    String def = node.has("definition") ? node.get("definition").asText() : "";

                    if (lowerQ.contains(term.toLowerCase()) || lowerQ.contains("what is") || lowerQ.contains("glossary")) {
                        matches = true;
                        snippet = term + ": " + def;
                        narrative = term + " is defined as: " + def;
                    }
                } else if (ev.getEventType() == EventType.TRIVIA) {
                    String q = node.has("question") ? node.get("question").asText() : "";
                    String note = node.has("triviaNote") ? node.get("triviaNote").asText() : "";

                    if (lowerQ.contains("trivia") || lowerQ.contains("behind the scenes") || lowerQ.contains("vfx") || lowerQ.contains("software")) {
                        matches = true;
                        snippet = q + " " + note;
                        narrative = "Production Fact: " + note;
                    }
                } else if (ev.getEventType() == EventType.SCENE) {
                    String desc = node.has("description") ? node.get("description").asText() : "";
                    if (lowerQ.contains("scene") || lowerQ.contains("happening") || lowerQ.contains("where") || titleLower.contains(lowerQ.split(" ")[0])) {
                        matches = true;
                        snippet = desc;
                        narrative = "In this scene (" + ev.getEventTitle() + "): " + desc;
                    }
                }
            } catch (Exception e) {
                log.warn("Error parsing payload for event {}: {}", ev.getId(), e.getMessage());
            }

            if (matches) {
                list.add(new MatchedEvent(ev, snippet, narrative));
            }
        }
        return list;
    }

    private String extractTopic(String question) {
        String lower = question.toLowerCase()
                .replaceAll("^(who is|what is|tell me about|explain|where is)\\s+", "")
                .replaceAll("[\\?\\.!]", "")
                .trim();
        return lower.isBlank() ? "this inquiry" : lower;
    }

    private static class MatchedEvent {
        final TimelineEvent event;
        final String snippet;
        final String narrative;

        MatchedEvent(TimelineEvent event, String snippet, String narrative) {
            this.event = event;
            this.snippet = snippet;
            this.narrative = narrative;
        }
    }
}
