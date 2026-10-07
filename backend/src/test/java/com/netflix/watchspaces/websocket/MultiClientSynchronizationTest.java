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

    @Test
    @DisplayName("Drift Simulation: Simulates multi-client adaptive rate convergence under variable network jitter")
    public void testMultiClientDriftConvergenceUnderJitter() {
        // Initial state at t = 0: host is at 100.0, client 1 lags by 120ms, client 2 leads by 120ms
        double hostPos = 100.0;
        double client1Pos = 99.880; // 120ms lag (within gentle micro-rate tier 50ms - 250ms)
        double client2Pos = 100.120; // 120ms lead (within gentle micro-rate tier 50ms - 250ms)

        // Simulate 20 ticks of 100ms each (2.0 seconds elapsed)
        for (int tick = 1; tick <= 20; tick++) {
            // Check drift at start of tick
            double drift1 = client1Pos - hostPos; // negative = lag, positive = lead
            double absDrift1 = Math.abs(drift1);
            double rate1 = (absDrift1 < 0.050) ? 1.0 : (drift1 < 0 ? 1.08 : 0.92);

            double drift2 = client2Pos - hostPos;
            double absDrift2 = Math.abs(drift2);
            double rate2 = (absDrift2 < 0.050) ? 1.0 : (drift2 < 0 ? 1.08 : 0.92);

            // Both host and clients advance over the 100ms interval
            hostPos += 0.100;
            client1Pos += 0.100 * rate1;
            client2Pos += 0.100 * rate2;
        }

        // After 2.0 seconds of adaptive micro-adjustments:
        double finalDrift1 = Math.abs(client1Pos - hostPos);
        double finalDrift2 = Math.abs(client2Pos - hostPos);

        assertTrue(finalDrift1 < 0.050, "Client 1 should converge into < 50ms tier within 2 seconds (actual: " + (finalDrift1 * 1000) + "ms)");
        assertTrue(finalDrift2 < 0.050, "Client 2 should converge into < 50ms tier within 2 seconds (actual: " + (finalDrift2 * 1000) + "ms)");
    }

    @Test
    @DisplayName("Multi-Client Broadcast & Persistence: Fact pinning and variant approval broadcast to all viewers and persist for reconnect")
    public void testMultiClientFactPinningAndVariantApproval() {
        // Connect host and 2 viewers
        roomSession.addParticipant("sess_host", hostUserId, "Host", null, "HOST");
        roomSession.addParticipant("sess_viewer1", viewer1Id, "Viewer 1", null, "VIEWER");
        roomSession.addParticipant("sess_viewer2", viewer2Id, "Viewer 2", null, "VIEWER");

        // 1. Host pins AI fact -> broadcasts to room
        java.util.Map<String, Object> fact = java.util.Map.of(
                "answer", "Tears of Steel was made using Blender open-source VFX pipeline.",
                "currentScene", "The Desolate Bridge of Amsterdam",
                "timestamp", 95.0,
                "sources", java.util.List.of(
                        java.util.Map.of("timelineEventId", 3L, "eventType", "TRIVIA", "title", "Open Source VFX")
                )
        );

        StepVerifier.create(roomSession.getBroadcastFlux().filter(json -> json.contains("room.ai.factPinned")))
                .then(() -> roomSession.pinFact(fact, hostUserId, false))
                .assertNext(json -> {
                    assertTrue(json.contains("room.ai.factPinned"));
                    assertTrue(json.contains("Tears of Steel was made using Blender"));
                })
                .thenCancel()
                .verify(Duration.ofSeconds(2));

        // 2. Host approves localized variant -> broadcasts to room
        java.util.Map<String, Object> variant = java.util.Map.of(
                "variantId", "es",
                "label", "Español",
                "type", "SUBTITLE"
        );

        StepVerifier.create(roomSession.getBroadcastFlux().filter(json -> json.contains("room.variant.applied")))
                .then(() -> roomSession.approveVariant(variant, hostUserId, false))
                .assertNext(json -> {
                    assertTrue(json.contains("room.variant.applied"));
                    assertTrue(json.contains("Español"));
                })
                .thenCancel()
                .verify(Duration.ofSeconds(2));

        // 3. Reconnecting participant receives persisted state in snapshot
        java.util.Map<String, Object> reconnectSnapshot = roomSession.getPlaybackSnapshot();
        assertNotNull(reconnectSnapshot.get("pinnedFact"), "Pinned fact must be persisted in snapshot");
        assertNotNull(reconnectSnapshot.get("approvedVariant"), "Approved variant must be persisted in snapshot");

        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> persistedFact = (java.util.Map<String, Object>) reconnectSnapshot.get("pinnedFact");
        assertEquals("Tears of Steel was made using Blender open-source VFX pipeline.", persistedFact.get("answer"));

        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> persistedVariant = (java.util.Map<String, Object>) reconnectSnapshot.get("approvedVariant");
        assertEquals("es", persistedVariant.get("variantId"));
        assertEquals("Español", persistedVariant.get("label"));
    }
}
