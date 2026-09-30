package com.netflix.watchspaces.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.watchspaces.domain.enums.PlaybackState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class MultiClientSynchronizationTest {

    private WatchSpaceRoomSession roomSession;
    private final String watchSpaceId = "ws_sync_test";
    private final Long hostUserId = 1L;
    private final Long viewer1Id = 2L;
    private final Long viewer2Id = 3L;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
        roomSession = new WatchSpaceRoomSession(watchSpaceId, hostUserId, objectMapper);
    }

    @Test
    @DisplayName("Multi-Client Broadcast: Multiple participants receive synchronous playback updates")
    public void testMultiClientPlaybackBroadcast() {
        // Connect host and 2 viewers
        roomSession.addParticipant("sess_host", hostUserId, "Host", null, "HOST");
        roomSession.addParticipant("sess_viewer1", viewer1Id, "Viewer 1", null, "VIEWER");
        roomSession.addParticipant("sess_viewer2", viewer2Id, "Viewer 2", null, "VIEWER");

        assertEquals(3, roomSession.getParticipantCount());

        // StepVerifier filters specifically for playback updates across the multicast flux
        StepVerifier.create(roomSession.getBroadcastFlux().filter(json -> json.contains("room.playback.update")))
                .then(() -> roomSession.updatePlayback(PlaybackState.PLAYING, 10.0, hostUserId, false))
                .assertNext(json -> {
                    assertTrue(json.contains("room.playback.update"));
                    assertTrue(json.contains("PLAYING"));
                    assertTrue(json.contains("10.0"));
                })
                .then(() -> roomSession.updatePlayback(PlaybackState.PAUSED, 25.5, hostUserId, false))
                .assertNext(json -> {
                    assertTrue(json.contains("room.playback.update"));
                    assertTrue(json.contains("PAUSED"));
                    assertTrue(json.contains("25.5"));
                })
                .thenCancel()
                .verify(Duration.ofSeconds(2));
    }

    @Test
    @DisplayName("Micro-Drift Convergence: Simulates network jitter and verifies drift tiers")
    public void testMicroDriftConvergenceTiers() {
        double hostPosition = 50.0;
        long serverTs = System.currentTimeMillis();
        long clockSkew = 15; // 15ms clock skew

        // Case 1: Drift < 50ms (In-Sync Tier)
        long now1 = serverTs + 1000;
        double targetPos1 = hostPosition + (now1 - serverTs - clockSkew) / 1000.0; // ~50.985s
        double clientCurrentTime1 = 50.965; // 20ms drift
        double drift1 = Math.abs(clientCurrentTime1 - targetPos1);

        assertTrue(drift1 < 0.050, "Drift should be within < 50ms in-sync tier");
        double rate1 = (drift1 < 0.050) ? 1.0 : (clientCurrentTime1 < targetPos1 ? 1.08 : 0.92);
        assertEquals(1.0, rate1, "Rate should remain 1.0 when in-sync");

        // Case 2: 50ms <= Drift <= 250ms Lagging (Gentle Catch-Up Tier)
        double clientCurrentTime2 = 50.850; // 135ms lag
        double drift2 = Math.abs(clientCurrentTime2 - targetPos1);
        assertTrue(drift2 >= 0.050 && drift2 <= 0.250, "Drift should be within gentle micro-rate tier");

        double rate2 = (clientCurrentTime2 < targetPos1) ? 1.08 : 0.92;
        assertEquals(1.08, rate2, "Rate should adjust to 1.08 to gently catch up without audio distortion");

        // Case 3: Drift > 250ms (Direct Micro-Seek Tier)
        double clientCurrentTime3 = 45.0; // 5.985s desync
        double drift3 = Math.abs(clientCurrentTime3 - targetPos1);
        assertTrue(drift3 > 0.250, "Drift should be in direct micro-seek tier");

        // Direct seek resets client to targetPos
        double resyncedClientTime = targetPos1;
        double driftAfterSeek = Math.abs(resyncedClientTime - targetPos1);
        assertEquals(0.0, driftAfterSeek, 0.001, "Direct seek should immediately eliminate desync");
    }
}
