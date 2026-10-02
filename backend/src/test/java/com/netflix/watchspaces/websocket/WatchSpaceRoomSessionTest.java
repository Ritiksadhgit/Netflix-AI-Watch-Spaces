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
}
