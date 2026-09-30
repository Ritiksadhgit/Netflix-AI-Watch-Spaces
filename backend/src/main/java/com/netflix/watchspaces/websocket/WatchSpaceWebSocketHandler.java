package com.netflix.watchspaces.websocket;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.watchspaces.domain.enums.MessageType;
import com.netflix.watchspaces.domain.enums.PlaybackState;
import com.netflix.watchspaces.domain.enums.RoleType;
import com.netflix.watchspaces.repository.WatchSpaceRepository;
import com.netflix.watchspaces.security.JwtTokenProvider;
import com.netflix.watchspaces.service.AiGroundingService;
import com.netflix.watchspaces.service.ChatService;
import com.netflix.watchspaces.service.NarrativeService;
import com.netflix.watchspaces.service.TimelineService;
import com.netflix.watchspaces.websocket.event.WebSocketEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.CloseStatus;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Component
public class WatchSpaceWebSocketHandler implements WebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(WatchSpaceWebSocketHandler.class);

    private final WatchSpaceRoomManager roomManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final ChatService chatService;
    private final TimelineService timelineService;
    private final NarrativeService narrativeService;
    private final AiGroundingService aiGroundingService;
    private final WatchSpaceRepository watchSpaceRepository;
    private final ObjectMapper objectMapper;

    public WatchSpaceWebSocketHandler(
            WatchSpaceRoomManager roomManager,
            JwtTokenProvider jwtTokenProvider,
            ChatService chatService,
            TimelineService timelineService,
            NarrativeService narrativeService,
            AiGroundingService aiGroundingService,
            WatchSpaceRepository watchSpaceRepository,
            ObjectMapper objectMapper) {
        this.roomManager = roomManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.chatService = chatService;
        this.timelineService = timelineService;
        this.narrativeService = narrativeService;
        this.aiGroundingService = aiGroundingService;
        this.watchSpaceRepository = watchSpaceRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        URI uri = session.getHandshakeInfo().getUri();
        Map<String, String> queryParams = parseQueryParams(uri.getQuery());

        String token = queryParams.get("token");
        String watchSpaceId = queryParams.get("watchSpaceId");

        if (watchSpaceId == null || watchSpaceId.isBlank()) {
            log.warn("WebSocket rejected: missing watchSpaceId in handshake query");
            return session.close(new CloseStatus(4400, "Missing watchSpaceId"));
        }

        if (token == null || !jwtTokenProvider.validateToken(token)) {
            log.warn("WebSocket rejected: invalid or missing JWT token");
            return session.close(CloseStatus.POLICY_VIOLATION);
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        String displayName = jwtTokenProvider.getDisplayNameFromToken(token);
        RoleType role = jwtTokenProvider.getRoleFromToken(token);
        boolean isAdmin = role == RoleType.ADMIN;

        WatchSpaceRoomSession room = roomManager.getOrCreateRoom(watchSpaceId);
        if (room.isLocked() && !userId.equals(room.getHostUserId()) && !isAdmin) {
            log.warn("User {} rejected: WatchSpace {} is locked", userId, watchSpaceId);
            return session.close(new CloseStatus(4403, "Room is locked by host"));
        }

        room.addParticipant(session.getId(), userId, displayName, null, role.name());

        // Dedicated unicast sink for session-direct replies (e.g. sync pong, rejected actions, AI answers)
        Sinks.Many<String> directReplySink = Sinks.many().unicast().onBackpressureBuffer();

        // 1. Initial Playback Snapshot on join
        String initialSnapshotJson = serializeEnvelope(WebSocketEnvelope.of(
                "room.playback.snapshot",
                watchSpaceId,
                room.getPlaybackSnapshot()
        ));

        // 2. Outgoing stream combining initial snapshot, direct replies, and room broadcasts
        Flux<WebSocketMessage> outputMessages = Flux.concat(
                Flux.just(session.textMessage(initialSnapshotJson)),
                Flux.merge(directReplySink.asFlux(), room.getBroadcastFlux())
                        .map(session::textMessage)
        );

        // 3. Incoming message processor
        Mono<Void> inputMessages = session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .flatMap(text -> handleIncomingMessage(text, room, userId, displayName, isAdmin, directReplySink, watchSpaceId))
                .doOnError(e -> log.error("Error receiving WebSocket message: {}", e.getMessage()))
                .then();

        return session.send(outputMessages)
                .and(inputMessages)
                .doFinally(signalType -> room.removeParticipant(session.getId()));
    }

    private Mono<Void> handleIncomingMessage(
            String text,
            WatchSpaceRoomSession room,
            Long userId,
            String displayName,
            boolean isAdmin,
            Sinks.Many<String> directReplySink,
            String watchSpaceId) {
        try {
            WebSocketEnvelope<Map<String, Object>> envelope = objectMapper.readValue(
                    text,
                    new TypeReference<>() {}
            );

            String event = envelope.getEvent();
            Map<String, Object> payload = envelope.getPayload() != null ? envelope.getPayload() : Map.of();

            if ("room.playback.update".equals(event)) {
                String stateStr = (String) payload.get("state");
                Number posNum = (Number) payload.get("position");

                PlaybackState state = stateStr != null ? PlaybackState.valueOf(stateStr) : PlaybackState.PAUSED;
                double position = posNum != null ? posNum.doubleValue() : 0.0;

                boolean success = room.updatePlayback(state, position, userId, isAdmin);
                if (!success) {
                    Map<String, Object> err = new HashMap<>();
                    err.put("reason", "Only the room host can control playback");
                    err.put("code", "FORBIDDEN_PLAYBACK_CONTROL");
                    directReplySink.tryEmitNext(serializeEnvelope(WebSocketEnvelope.of(
                            "room.action.rejected",
                            watchSpaceId,
                            err
                    )));
                } else {
                    // Check and trigger synchronized trivia and narrative variation points
                    return timelineService.checkAndTriggerTrivia(watchSpaceId, position)
                            .then(narrativeService.checkAndTriggerVariation(watchSpaceId, position));
                }
            } else if ("room.sync.ping".equals(event)) {
                Number clientSendTs = (Number) payload.get("clientSendTs");
                Map<String, Object> pong = new HashMap<>();
                pong.put("clientSendTs", clientSendTs != null ? clientSendTs.longValue() : 0L);
                pong.put("serverTs", System.currentTimeMillis());

                directReplySink.tryEmitNext(serializeEnvelope(WebSocketEnvelope.of(
                        "room.sync.pong",
                        watchSpaceId,
                        pong
                )));
            } else if ("room.chat.message".equals(event)) {
                if (room.isUserMuted(userId)) {
                    Map<String, Object> err = new HashMap<>();
                    err.put("reason", "You are muted in this room by the host");
                    err.put("code", "FORBIDDEN_MUTED");
                    directReplySink.tryEmitNext(serializeEnvelope(WebSocketEnvelope.of(
                            "room.action.rejected",
                            watchSpaceId,
                            err
                    )));
                    return Mono.empty();
                }

                String body = (String) payload.get("body");
                Number tsSecondsNum = (Number) payload.get("tsSeconds");
                Double tsSeconds = tsSecondsNum != null ? tsSecondsNum.doubleValue() : room.getPlaybackPositionSeconds();

                return chatService.saveMessage(watchSpaceId, userId, displayName, body, tsSeconds, MessageType.USER)
                        .doOnSuccess(saved -> {
                            Map<String, Object> msgPayload = new HashMap<>();
                            msgPayload.put("id", saved.getId());
                            msgPayload.put("userId", userId);
                            msgPayload.put("senderName", displayName);
                            msgPayload.put("msgType", "USER");
                            msgPayload.put("body", saved.getBody());
                            msgPayload.put("tsSeconds", saved.getTsSeconds());
                            msgPayload.put("createdAt", System.currentTimeMillis());

                            room.broadcast(WebSocketEnvelope.of("room.chat.message", watchSpaceId, msgPayload));
                        })
                        .doOnError(e -> log.error("Failed to save chat message: {}", e.getMessage()))
                        .then();

            } else if ("room.chat.typing".equals(event)) {
                Map<String, Object> typingPayload = new HashMap<>();
                typingPayload.put("userId", userId);
                typingPayload.put("displayName", displayName);
                typingPayload.put("isTyping", payload.get("isTyping"));
                room.broadcast(WebSocketEnvelope.of("room.chat.typing", watchSpaceId, typingPayload));
            } else if ("room.ai.ask".equals(event)) {
                String question = (String) payload.get("question");
                Number tsNum = (Number) payload.get("currentTimestamp");
                double ts = tsNum != null ? tsNum.doubleValue() : room.getPlaybackPositionSeconds();

                return Mono.fromCallable(() -> watchSpaceRepository.findById(watchSpaceId))
                        .subscribeOn(Schedulers.boundedElastic())
                        .flatMap(spaceOpt -> {
                            if (spaceOpt.isPresent() && spaceOpt.get().getTitle() != null) {
                                Long titleId = spaceOpt.get().getTitle().getId();
                                String verbosity = spaceOpt.get().getAiVerbosity() != null
                                        ? spaceOpt.get().getAiVerbosity().name() : "NORMAL";
                                return aiGroundingService.answerQuestion(titleId, question, ts, verbosity);
                            }
                            return Mono.empty();
                        })
                        .doOnSuccess(ans -> {
                            if (ans != null) {
                                directReplySink.tryEmitNext(serializeEnvelope(WebSocketEnvelope.of(
                                        "room.ai.answer",
                                        watchSpaceId,
                                        ans
                                )));
                            }
                        })
                        .then();
            } else if ("room.variation.voteCast".equals(event)) {
                Number variationIdNum = (Number) payload.get("variationId");
                Number optionIdNum = (Number) payload.get("optionId");
                Long variationId = variationIdNum != null ? variationIdNum.longValue() : 0L;
                Long optionId = optionIdNum != null ? optionIdNum.longValue() : 0L;

                return narrativeService.castVote(watchSpaceId, userId, variationId, optionId).then();
            } else if ("room.variation.finalize".equals(event)) {
                Number variationIdNum = (Number) payload.get("variationId");
                Long variationId = variationIdNum != null ? variationIdNum.longValue() : 0L;
                return narrativeService.finalizeVote(watchSpaceId, variationId).then();
            } else if ("room.moderation.action".equals(event)) {
                boolean isHost = userId.equals(room.getHostUserId());
                if (!isHost && !isAdmin) {
                    Map<String, Object> err = new HashMap<>();
                    err.put("reason", "Only the host or admin can perform moderation actions");
                    err.put("code", "FORBIDDEN_MODERATION");
                    directReplySink.tryEmitNext(serializeEnvelope(WebSocketEnvelope.of(
                            "room.action.rejected",
                            watchSpaceId,
                            err
                    )));
                    return Mono.empty();
                }

                String action = (String) payload.get("action");
                Number targetUserIdNum = (Number) payload.get("targetUserId");
                Long targetUserId = targetUserIdNum != null ? targetUserIdNum.longValue() : null;

                if ("MUTE".equalsIgnoreCase(action) && targetUserId != null) {
                    room.muteUser(targetUserId, true);
                    room.broadcast(WebSocketEnvelope.of("room.moderation.action", watchSpaceId, Map.of(
                            "action", "MUTE",
                            "targetUserId", targetUserId
                    )));
                } else if ("UNMUTE".equalsIgnoreCase(action) && targetUserId != null) {
                    room.muteUser(targetUserId, false);
                    room.broadcast(WebSocketEnvelope.of("room.moderation.action", watchSpaceId, Map.of(
                            "action", "UNMUTE",
                            "targetUserId", targetUserId
                    )));
                } else if ("TRANSFER_HOST".equalsIgnoreCase(action) && targetUserId != null) {
                    room.transferHost(targetUserId);
                    room.broadcast(WebSocketEnvelope.of("room.moderation.action", watchSpaceId, Map.of(
                            "action", "HOST_TRANSFERRED",
                            "newHostUserId", targetUserId
                    )));
                } else if ("LOCK".equalsIgnoreCase(action)) {
                    Boolean locked = (Boolean) payload.get("locked");
                    boolean lockVal = Boolean.TRUE.equals(locked);
                    room.setLocked(lockVal);
                    room.broadcast(WebSocketEnvelope.of("room.moderation.action", watchSpaceId, Map.of(
                            "action", "ROOM_LOCK",
                            "isLocked", lockVal
                    )));
                } else if ("KICK".equalsIgnoreCase(action) && targetUserId != null) {
                    room.broadcast(WebSocketEnvelope.of("room.moderation.action", watchSpaceId, Map.of(
                            "action", "KICK",
                            "targetUserId", targetUserId
                    )));
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse incoming WebSocket message: {}", e.getMessage());
        }
        return Mono.empty();
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isBlank()) return map;

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx > 0 && idx < pair.length() - 1) {
                map.put(pair.substring(0, idx), pair.substring(idx + 1));
            }
        }
        return map;
    }

    private String serializeEnvelope(WebSocketEnvelope<?> envelope) {
        try {
            return objectMapper.writeValueAsString(envelope);
        } catch (Exception e) {
            return "{}";
        }
    }
}
