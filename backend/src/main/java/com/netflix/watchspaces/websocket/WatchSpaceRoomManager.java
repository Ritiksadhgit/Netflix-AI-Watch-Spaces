package com.netflix.watchspaces.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.watchspaces.domain.entity.WatchSpace;
import com.netflix.watchspaces.domain.enums.PlaybackState;
import com.netflix.watchspaces.repository.WatchSpaceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WatchSpaceRoomManager {

    private static final Logger log = LoggerFactory.getLogger(WatchSpaceRoomManager.class);

    private final WatchSpaceRepository watchSpaceRepository;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, WatchSpaceRoomSession> rooms = new ConcurrentHashMap<>();

    public WatchSpaceRoomManager(WatchSpaceRepository watchSpaceRepository, ObjectMapper objectMapper) {
        this.watchSpaceRepository = watchSpaceRepository;
        this.objectMapper = objectMapper;
    }

    public WatchSpaceRoomSession getOrCreateRoom(String watchSpaceId) {
        return rooms.computeIfAbsent(watchSpaceId, id -> {
            Optional<WatchSpace> spaceOpt = watchSpaceRepository.findById(id);
            Long hostId = spaceOpt.map(ws -> ws.getHostUser().getId()).orElse(1L);
            WatchSpaceRoomSession session = new WatchSpaceRoomSession(id, hostId, objectMapper);
            if (spaceOpt.isPresent()) {
                WatchSpace ws = spaceOpt.get();
                PlaybackState state = ws.getPlaybackState() != null ? ws.getPlaybackState() : PlaybackState.PAUSED;
                double position = ws.getPlaybackPositionSeconds() != null ? ws.getPlaybackPositionSeconds() : 0.0;
                session.setInitialPlayback(state, position);
                session.setLocked(ws.isLocked());
            }
            log.info("Initialized in-memory WatchSpaceRoomSession for ID: {} with host: {}", id, hostId);
            return session;
        });
    }

    public Optional<WatchSpaceRoomSession> getRoom(String watchSpaceId) {
        return Optional.ofNullable(rooms.get(watchSpaceId));
    }

    public void removeRoom(String watchSpaceId) {
        WatchSpaceRoomSession removed = rooms.remove(watchSpaceId);
        if (removed != null) {
            log.info("Evicted WatchSpaceRoomSession for ID: {}", watchSpaceId);
        }
    }
}
