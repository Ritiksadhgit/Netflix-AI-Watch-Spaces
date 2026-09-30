package com.netflix.watchspaces.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.watchspaces.domain.enums.PlaybackState;
import com.netflix.watchspaces.websocket.event.WebSocketEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WatchSpaceRoomSession {

    private static final Logger log = LoggerFactory.getLogger(WatchSpaceRoomSession.class);

    private final String watchSpaceId;
    private Long hostUserId;
    private final ObjectMapper objectMapper;

    private PlaybackState playbackState = PlaybackState.PAUSED;
    private double playbackPositionSeconds = 0.0;
    private long playbackUpdatedAt = System.currentTimeMillis();
    private boolean isLocked = false;

    // Sinks for non-blocking reactive multicasting
    private final Sinks.Many<String> broadcastSink =
            Sinks.many().multicast().onBackpressureBuffer(256, false);

    // Connected participants: sessionId -> ParticipantInfo
    private final ConcurrentHashMap<String, ParticipantInfo> sessions = new ConcurrentHashMap<>();

    // userId -> sessionId mapping for deduplication & takeover
    private final ConcurrentHashMap<Long, String> userSessionMap = new ConcurrentHashMap<>();

    // Muted user IDs
    private final Set<Long> mutedUserIds = Collections.synchronizedSet(new HashSet<>());

    // Triggered trivia IDs to prevent duplicates
    private final Set<Long> triggeredTriviaEventIds = Collections.synchronizedSet(new HashSet<>());

    public WatchSpaceRoomSession(String watchSpaceId, Long hostUserId, ObjectMapper objectMapper) {
        this.watchSpaceId = watchSpaceId;
        this.hostUserId = hostUserId;
        this.objectMapper = objectMapper;
    }

    public Flux<String> getBroadcastFlux() {
        return broadcastSink.asFlux();
    }

    public synchronized void setInitialPlayback(PlaybackState state, double position) {
        this.playbackState = state != null ? state : PlaybackState.PAUSED;
        this.playbackPositionSeconds = Math.max(0.0, position);
        this.playbackUpdatedAt = System.currentTimeMillis();
    }

    public synchronized boolean updatePlayback(PlaybackState newState, double newPosition, Long senderUserId, boolean isAdmin) {
        if (!senderUserId.equals(hostUserId) && !isAdmin) {
            log.warn("Unauthorized playback attempt on room {} by user {}", watchSpaceId, senderUserId);
            return false;
        }

        this.playbackState = newState != null ? newState : PlaybackState.PAUSED;
        this.playbackPositionSeconds = Math.max(0.0, newPosition);
        this.playbackUpdatedAt = System.currentTimeMillis();

        Map<String, Object> payload = new HashMap<>();
        payload.put("state", this.playbackState.name());
        payload.put("position", this.playbackPositionSeconds);
        payload.put("serverTs", this.playbackUpdatedAt);
        payload.put("rate", 1.0);

        broadcast(WebSocketEnvelope.of("room.playback.update", watchSpaceId, payload));
        return true;
    }

    public synchronized void addParticipant(String sessionId, Long userId, String displayName, String avatarUrl, String role) {
        // Prevent duplicate joins: check if user already connected on another tab
        String existingSessionId = userSessionMap.put(userId, sessionId);
        if (existingSessionId != null && !existingSessionId.equals(sessionId)) {
            log.info("User {} connected from new tab/session {}. Superseding old session {}", userId, sessionId, existingSessionId);
            sessions.remove(existingSessionId);
        }

        boolean isHost = userId.equals(hostUserId);
        sessions.put(sessionId, new ParticipantInfo(userId, displayName, avatarUrl, role, isHost));

        broadcastPresence();
    }

    public synchronized void removeParticipant(String sessionId) {
        ParticipantInfo removed = sessions.remove(sessionId);
        if (removed != null) {
            userSessionMap.remove(removed.getUserId(), sessionId);
            log.info("Participant {} left room {}. Active count: {}", removed.getDisplayName(), watchSpaceId, sessions.size());
            broadcastPresence();
        }
    }

    public synchronized void muteUser(Long userId, boolean muted) {
        if (muted) {
            mutedUserIds.add(userId);
        } else {
            mutedUserIds.remove(userId);
        }
        broadcastPresence();
    }

    public boolean isUserMuted(Long userId) {
        return mutedUserIds.contains(userId);
    }

    public synchronized void transferHost(Long newHostUserId) {
        this.hostUserId = newHostUserId;
        log.info("Host transferred to user {} in room {}", newHostUserId, watchSpaceId);
        broadcastPresence();
    }

    public void setLocked(boolean locked) {
        this.isLocked = locked;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public String getSessionIdForUser(Long userId) {
        return userSessionMap.get(userId);
    }

    public void broadcast(WebSocketEnvelope<?> envelope) {
        try {
            String json = objectMapper.writeValueAsString(envelope);
            broadcastSink.tryEmitNext(json);
        } catch (Exception e) {
            log.error("Failed to serialize WebSocket envelope: {}", e.getMessage());
        }
    }

    public void broadcastPresence() {
        List<Map<String, Object>> roster = new ArrayList<>();
        for (ParticipantInfo p : sessions.values()) {
            Map<String, Object> item = new HashMap<>();
            item.put("userId", p.getUserId());
            item.put("displayName", p.getDisplayName());
            item.put("avatarUrl", p.getAvatarUrl());
            item.put("role", p.getRole());
            item.put("isHost", p.getUserId().equals(hostUserId));
            item.put("isMuted", mutedUserIds.contains(p.getUserId()));
            item.put("onlineStatus", "ONLINE");
            roster.add(item);
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("participants", roster);
        payload.put("count", roster.size());
        payload.put("hostUserId", hostUserId);
        payload.put("isLocked", isLocked);

        broadcast(WebSocketEnvelope.of("room.presence.update", watchSpaceId, payload));
    }

    public Map<String, Object> getPlaybackSnapshot() {
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("state", playbackState.name());
        snapshot.put("position", playbackPositionSeconds);
        snapshot.put("serverTs", playbackUpdatedAt);
        snapshot.put("hostUserId", hostUserId);
        snapshot.put("isLocked", isLocked);
        return snapshot;
    }

    public boolean isTriviaTriggered(Long triviaId) {
        return triggeredTriviaEventIds.contains(triviaId);
    }

    public void markTriviaTriggered(Long triviaId) {
        triggeredTriviaEventIds.add(triviaId);
    }

    public String getWatchSpaceId() { return watchSpaceId; }
    public Long getHostUserId() { return hostUserId; }
    public void setHostUserId(Long hostUserId) { this.hostUserId = hostUserId; }
    public PlaybackState getPlaybackState() { return playbackState; }
    public double getPlaybackPositionSeconds() { return playbackPositionSeconds; }
    public long getPlaybackUpdatedAt() { return playbackUpdatedAt; }
    public int getParticipantCount() { return sessions.size(); }

    public static class ParticipantInfo {
        private final Long userId;
        private final String displayName;
        private final String avatarUrl;
        private final String role;
        private final boolean isHost;

        public ParticipantInfo(Long userId, String displayName, String avatarUrl, String role, boolean isHost) {
            this.userId = userId;
            this.displayName = displayName;
            this.avatarUrl = avatarUrl;
            this.role = role;
            this.isHost = isHost;
        }

        public Long getUserId() { return userId; }
        public String getDisplayName() { return displayName; }
        public String getAvatarUrl() { return avatarUrl; }
        public String getRole() { return role; }
        public boolean isHost() { return isHost; }
    }
}
