package com.netflix.watchspaces.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.watchspaces.domain.dto.response.VariationAppliedResponse;
import com.netflix.watchspaces.domain.dto.response.VariationOptionResponse;
import com.netflix.watchspaces.domain.dto.response.VariationVoteOpenResponse;
import com.netflix.watchspaces.domain.entity.TimelineEvent;
import com.netflix.watchspaces.domain.entity.VariationOption;
import com.netflix.watchspaces.domain.entity.WatchSpace;
import com.netflix.watchspaces.domain.enums.EventType;
import com.netflix.watchspaces.repository.TimelineEventRepository;
import com.netflix.watchspaces.repository.VariationOptionRepository;
import com.netflix.watchspaces.repository.WatchSpaceRepository;
import com.netflix.watchspaces.websocket.WatchSpaceRoomManager;
import com.netflix.watchspaces.websocket.WatchSpaceRoomSession;
import com.netflix.watchspaces.websocket.event.WebSocketEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class NarrativeService {

    private static final Logger log = LoggerFactory.getLogger(NarrativeService.class);

    private final TimelineEventRepository timelineEventRepository;
    private final VariationOptionRepository variationOptionRepository;
    private final WatchSpaceRepository watchSpaceRepository;
    private final WatchSpaceRoomManager roomManager;
    private final ObjectMapper objectMapper;

    public NarrativeService(
            TimelineEventRepository timelineEventRepository,
            VariationOptionRepository variationOptionRepository,
            WatchSpaceRepository watchSpaceRepository,
            WatchSpaceRoomManager roomManager,
            ObjectMapper objectMapper) {
        this.timelineEventRepository = timelineEventRepository;
        this.variationOptionRepository = variationOptionRepository;
        this.watchSpaceRepository = watchSpaceRepository;
        this.roomManager = roomManager;
        this.objectMapper = objectMapper;
    }

    public Flux<VariationVoteOpenResponse> getVariationsForTitle(Long titleId) {
        return Mono.fromCallable(() -> {
            List<TimelineEvent> events = timelineEventRepository.findByTitleIdAndEventType(titleId, EventType.VARIATION);
            List<VariationVoteOpenResponse> list = new ArrayList<>();
            for (TimelineEvent ev : events) {
                list.add(buildVoteOpenResponse(ev));
            }
            return list;
        }).subscribeOn(Schedulers.boundedElastic()).flatMapMany(Flux::fromIterable);
    }

    public Mono<Void> checkAndTriggerVariation(String watchSpaceId, double currentPosition) {
        return Mono.fromRunnable(() -> {
            Optional<WatchSpaceRoomSession> roomOpt = roomManager.getRoom(watchSpaceId);
            if (roomOpt.isEmpty()) return;

            WatchSpaceRoomSession room = roomOpt.get();
            if (!room.isVotingEnabled()) return;

            Optional<WatchSpace> spaceOpt = watchSpaceRepository.findById(watchSpaceId);
            if (spaceOpt.isEmpty() || spaceOpt.get().getTitle() == null) return;

            Long titleId = spaceOpt.get().getTitle().getId();
            List<TimelineEvent> variationEvents = timelineEventRepository.findByTitleIdAndEventType(titleId, EventType.VARIATION);

            for (TimelineEvent ev : variationEvents) {
                double diff = currentPosition - ev.getTsSeconds();

                // Trigger window: within [0s, 3s] after passing the variation marker
                if (diff >= 0 && diff <= 3.0) {
                    if (!room.isVariationTriggered(ev.getId())) {
                        room.markVariationTriggered(ev.getId());

                        VariationVoteOpenResponse voteOpen = buildVoteOpenResponse(ev);
                        Map<Long, String> labels = new HashMap<>();
                        Map<Long, String> assets = new HashMap<>();
                        for (VariationOptionResponse opt : voteOpen.getOptions()) {
                            labels.put(opt.getId(), opt.getLabel());
                            assets.put(opt.getId(), opt.getAssetRef());
                        }

                        room.startVotingSession(
                                ev.getId(),
                                voteOpen.getPrompt(),
                                labels,
                                assets,
                                voteOpen.getDurationSeconds()
                        );

                        log.info("Opened narrative voting for variation {} in room {}", ev.getId(), watchSpaceId);
                        room.broadcast(WebSocketEnvelope.of("room.variation.voteOpen", watchSpaceId, voteOpen));
                    }
                }
            }
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    public Mono<Map<String, Object>> castVote(String watchSpaceId, Long userId, Long variationId, Long optionId) {
        return Mono.fromCallable(() -> {
            Optional<WatchSpaceRoomSession> roomOpt = roomManager.getRoom(watchSpaceId);
            if (roomOpt.isEmpty()) {
                return Map.<String, Object>of("success", false);
            }

            WatchSpaceRoomSession room = roomOpt.get();
            boolean recorded = room.recordVote(userId, optionId);

            WatchSpaceRoomSession.VotingSession session = room.getActiveVotingSession();
            if (session != null && recorded) {
                Map<Long, Integer> counts = session.getVoteCounts();

                List<Map<String, Object>> optionsList = new ArrayList<>();
                session.getOptionLabels().forEach((optId, label) -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", optId);
                    item.put("label", label);
                    item.put("votes", counts.getOrDefault(optId, 0));
                    optionsList.add(item);
                });

                Map<String, Object> tallyPayload = new HashMap<>();
                tallyPayload.put("variationId", variationId);
                tallyPayload.put("totalVotes", session.getTotalVotes());
                tallyPayload.put("options", optionsList);

                room.broadcast(WebSocketEnvelope.of("room.variation.voteCast", watchSpaceId, tallyPayload));
                return tallyPayload;
            }

            return Map.<String, Object>of("success", false);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public Mono<VariationAppliedResponse> finalizeVote(String watchSpaceId, Long variationId) {
        return Mono.fromCallable(() -> {
            Optional<WatchSpaceRoomSession> roomOpt = roomManager.getRoom(watchSpaceId);
            if (roomOpt.isEmpty()) return null;

            WatchSpaceRoomSession room = roomOpt.get();
            WatchSpaceRoomSession.VotingSession session = room.finalizeVoting();
            if (session == null) return null;

            Long winningOptionId = session.getWinningOptionId();
            String winningLabel = session.getOptionLabels().get(winningOptionId);
            String assetRef = session.getOptionAssetRefs().get(winningOptionId);
            int totalVotes = session.getTotalVotes();

            // Persist winning votes to DB
            if (winningOptionId != null) {
                variationOptionRepository.findById(winningOptionId).ifPresent(opt -> {
                    opt.setVoteCount(opt.getVoteCount() + session.getVoteCounts().getOrDefault(winningOptionId, 0));
                    variationOptionRepository.save(opt);
                });
            }

            VariationAppliedResponse applied = new VariationAppliedResponse(
                    variationId,
                    winningOptionId,
                    winningLabel,
                    totalVotes,
                    assetRef,
                    "NARRATIVE_BRANCH_APPLIED"
            );

            log.info("Finalized narrative voting in room {}: Option '{}' won with {} votes", watchSpaceId, winningLabel, totalVotes);
            room.broadcast(WebSocketEnvelope.of("room.variation.applied", watchSpaceId, applied));

            return applied;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private VariationVoteOpenResponse buildVoteOpenResponse(TimelineEvent ev) {
        String prompt = "Narrative branch point ahead. Cast your vote!";
        int durationSec = 20;

        try {
            JsonNode node = objectMapper.readTree(ev.getPayloadJson());
            if (node.has("prompt")) prompt = node.get("prompt").asText();
            if (node.has("durationSeconds")) durationSec = node.get("durationSeconds").asInt();
        } catch (Exception e) {
            log.warn("Failed to parse variation payload for event {}: {}", ev.getId(), e.getMessage());
        }

        List<VariationOption> options = variationOptionRepository.findByTimelineEventId(ev.getId());
        List<VariationOptionResponse> optionDtos = options.stream()
                .map(VariationOptionResponse::fromEntity)
                .collect(Collectors.toList());

        long expiresAt = System.currentTimeMillis() + (durationSec * 1000L);
        return new VariationVoteOpenResponse(ev.getId(), prompt, optionDtos, durationSec, expiresAt);
    }
}
