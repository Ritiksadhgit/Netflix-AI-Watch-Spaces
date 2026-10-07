package com.netflix.watchspaces.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.watchspaces.domain.enums.PlaybackState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WatchSpaceRoomSessionTest {

    private WatchSpaceRoomSession roomSession;
    private final String roomSpaceId = "ws_test_123";
    private final Long hostUserId = 42L;
    private final Long viewerUserId = 99L;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        roomSession = new WatchSpaceRoomSession(roomSpaceId, hostUserId, objectMapper);
    }

    @Test
    @DisplayName("Host should successfully update authoritative playback state")
    void testHostUpdatesPlayback() {
        boolean success = roomSession.updatePlayback(PlaybackState.PLAYING, 120.5, hostUserId, false);

        assertTrue(success);
        assertEquals(PlaybackState.PLAYING, roomSession.getPlaybackState());
        assertEquals(120.5, roomSession.getPlaybackPositionSeconds());
        assertTrue(roomSession.getPlaybackUpdatedAt() > 0);
    }

    @Test
    @DisplayName("Non-host viewer playback update attempt must be rejected")
    void testViewerPlaybackUpdateRejected() {
        boolean success = roomSession.updatePlayback(PlaybackState.PLAYING, 200.0, viewerUserId, false);

        assertFalse(success);
        // State remains unchanged
        assertEquals(PlaybackState.PAUSED, roomSession.getPlaybackState());
        assertEquals(0.0, roomSession.getPlaybackPositionSeconds());
    }

    @Test
    @DisplayName("Admin user should be allowed to update playback even if not original host")
    void testAdminCanControlPlayback() {
        boolean success = roomSession.updatePlayback(PlaybackState.PLAYING, 55.0, viewerUserId, true);

        assertTrue(success);
        assertEquals(PlaybackState.PLAYING, roomSession.getPlaybackState());
        assertEquals(55.0, roomSession.getPlaybackPositionSeconds());
    }

    @Test
    @DisplayName("Duplicate session from same user should supersede without double counting")
    void testDuplicateSessionSuperseded() {
        roomSession.addParticipant("sess_1", viewerUserId, "Alex", null, "VIEWER");
        assertEquals(1, roomSession.getParticipantCount());

        // Connect again with same userId on new tab
        roomSession.addParticipant("sess_2", viewerUserId, "Alex", null, "VIEWER");
        assertEquals(1, roomSession.getParticipantCount());
    }

    @Test
    @DisplayName("Broadcast flux should emit envelope when playback updates")
    void testBroadcastEmittedOnPlaybackUpdate() {
        StepVerifier.create(roomSession.getBroadcastFlux())
                .then(() -> roomSession.updatePlayback(PlaybackState.PLAYING, 30.0, hostUserId, false))
                .assertNext(json -> {
                    assertTrue(json.contains("room.playback.update"));
                    assertTrue(json.contains("PLAYING"));
                    assertTrue(json.contains("30.0"));
                })
                .thenCancel()
                .verify();
    }

    @Test
    @DisplayName("Mute and Unmute participant reflects in room state and presence")
    void testMuteAndUnmuteParticipant() {
        roomSession.addParticipant("sess_1", viewerUserId, "Alex", null, "VIEWER");
        assertFalse(roomSession.isUserMuted(viewerUserId));

        // Host mutes participant
        roomSession.muteUser(viewerUserId, true);
        assertTrue(roomSession.isUserMuted(viewerUserId));

        // Host unmutes participant
        roomSession.muteUser(viewerUserId, false);
        assertFalse(roomSession.isUserMuted(viewerUserId));
    }

    @Test
    @DisplayName("Transfer host updates authoritative host and presence")
    void testTransferHost() {
        roomSession.addParticipant("sess_1", hostUserId, "Elena", null, "HOST");
        roomSession.addParticipant("sess_2", viewerUserId, "Alex", null, "VIEWER");

        assertEquals(hostUserId, roomSession.getHostUserId());

        // Transfer host to viewerUserId
        roomSession.transferHost(viewerUserId);
        assertEquals(viewerUserId, roomSession.getHostUserId());

        // Now new host can control playback
        boolean success = roomSession.updatePlayback(PlaybackState.PLAYING, 45.0, viewerUserId, false);
        assertTrue(success);
        assertEquals(PlaybackState.PLAYING, roomSession.getPlaybackState());

        // Old host cannot control playback anymore
        boolean oldHostAttempt = roomSession.updatePlayback(PlaybackState.PAUSED, 60.0, hostUserId, false);
        assertFalse(oldHostAttempt);
    }

    @Test
    @DisplayName("Lock room updates locked flag and snapshot")
    void testLockRoom() {
        assertFalse(roomSession.isLocked());

        roomSession.setLocked(true);
        assertTrue(roomSession.isLocked());

        Map<String, Object> snapshot = roomSession.getPlaybackSnapshot();
        assertEquals(true, snapshot.get("isLocked"));
    }

    @Test
    @DisplayName("getPresencePayload returns accurate roster snapshot on join and disconnect")
    void testGetPresencePayload() {
        roomSession.addParticipant("sess_host", hostUserId, "Elena (Party Host)", null, "HOST");
        roomSession.addParticipant("sess_viewer", viewerUserId, "Marcus (Viewer)", null, "VIEWER");

        Map<String, Object> presence = roomSession.getPresencePayload();
        assertNotNull(presence);
        assertEquals(2, presence.get("count"));
        assertEquals(hostUserId, presence.get("hostUserId"));

        @SuppressWarnings("unchecked")
        java.util.List<Map<String, Object>> participants = (java.util.List<Map<String, Object>>) presence.get("participants");
        assertEquals(2, participants.size());

        // Participant leaves
        roomSession.removeParticipant("sess_viewer");
        Map<String, Object> presenceAfterLeave = roomSession.getPresencePayload();
        assertEquals(1, presenceAfterLeave.get("count"));
    }

    @Test
    @DisplayName("Host can pin fact and it persists in session snapshot")
    void testHostPinsFactSuccessfully() {
        Map<String, Object> fact = Map.of(
                "answer", "Thom retrofitted his cybernetic arm after the Fall of Amsterdam.",
                "currentScene", "The Desolate Bridge of Amsterdam",
                "timestamp", 45.0,
                "sources", java.util.List.of(
                        Map.of("timelineEventId", 2L, "title", "Thom (The Lead Pilot)", "eventType", "CHARACTER")
                )
        );

        boolean success = roomSession.pinFact(fact, hostUserId, false);
        assertTrue(success);
        assertNotNull(roomSession.getPinnedFact());
        assertEquals("Thom retrofitted his cybernetic arm after the Fall of Amsterdam.", roomSession.getPinnedFact().get("answer"));

        Map<String, Object> snapshot = roomSession.getPlaybackSnapshot();
        assertNotNull(snapshot.get("pinnedFact"));
        assertEquals(fact, snapshot.get("pinnedFact"));
    }

    @Test
    @DisplayName("Viewer cannot pin fact (rejected server-side)")
    void testViewerPinFactRejected() {
        Map<String, Object> fact = Map.of(
                "answer", "Unverified fact from viewer",
                "currentScene", "Scene 1"
        );

        boolean success = roomSession.pinFact(fact, viewerUserId, false);
        assertFalse(success);
        assertNull(roomSession.getPinnedFact());
        assertNull(roomSession.getPlaybackSnapshot().get("pinnedFact"));
    }

    @Test
    @DisplayName("Host can unpin fact")
    void testHostUnpinsFact() {
        Map<String, Object> fact = Map.of("answer", "Fact to be unpinned");
        roomSession.pinFact(fact, hostUserId, false);
        assertNotNull(roomSession.getPinnedFact());

        boolean unpinned = roomSession.unpinFact(hostUserId, false);
        assertTrue(unpinned);
        assertNull(roomSession.getPinnedFact());
        assertNull(roomSession.getPlaybackSnapshot().get("pinnedFact"));
    }

    @Test
    @DisplayName("Broadcast emitted when Host pins fact")
    void testBroadcastEmittedOnPinFact() {
        Map<String, Object> fact = Map.of("answer", "Broadcast fact");

        StepVerifier.create(roomSession.getBroadcastFlux())
                .then(() -> roomSession.pinFact(fact, hostUserId, false))
                .assertNext(json -> {
                    assertTrue(json.contains("room.ai.factPinned"));
                    assertTrue(json.contains("Broadcast fact"));
                })
                .thenCancel()
                .verify();
    }

    @Test
    @DisplayName("Host can approve localized variant and it persists in session snapshot")
    void testHostApprovesLocalizedVariantSuccessfully() {
        Map<String, Object> variant = Map.of(
                "variantId", "es",
                "label", "Español",
                "type", "SUBTITLE"
        );

        boolean success = roomSession.approveVariant(variant, hostUserId, false);
        assertTrue(success);
        assertNotNull(roomSession.getApprovedVariant());
        assertEquals("es", roomSession.getApprovedVariant().get("variantId"));
        assertEquals("Español", roomSession.getApprovedVariant().get("label"));

        Map<String, Object> snapshot = roomSession.getPlaybackSnapshot();
        assertNotNull(snapshot.get("approvedVariant"));
        assertEquals(variant, snapshot.get("approvedVariant"));
    }

    @Test
    @DisplayName("Viewer cannot approve variant (rejected server-side)")
    void testViewerApproveVariantRejected() {
        Map<String, Object> variant = Map.of(
                "variantId", "fr",
                "label", "Français",
                "type", "SUBTITLE"
        );

        boolean success = roomSession.approveVariant(variant, viewerUserId, false);
        assertFalse(success);
        assertNull(roomSession.getApprovedVariant());
        assertNull(roomSession.getPlaybackSnapshot().get("approvedVariant"));
    }

    @Test
    @DisplayName("Broadcast emitted when Host approves localized variant")
    void testBroadcastEmittedOnVariantApproved() {
        Map<String, Object> variant = Map.of("variantId", "es", "label", "Español");

        StepVerifier.create(roomSession.getBroadcastFlux())
                .then(() -> roomSession.approveVariant(variant, hostUserId, false))
                .assertNext(json -> {
                    assertTrue(json.contains("room.variant.applied"));
                    assertTrue(json.contains("Español"));
                })
                .thenCancel()
                .verify();
    }
}
