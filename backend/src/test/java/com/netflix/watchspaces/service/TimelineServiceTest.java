package com.netflix.watchspaces.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.watchspaces.domain.entity.TimelineEvent;
import com.netflix.watchspaces.domain.entity.Title;
import com.netflix.watchspaces.domain.entity.WatchSpace;
import com.netflix.watchspaces.domain.enums.EventType;
import com.netflix.watchspaces.repository.TimelineEventRepository;
import com.netflix.watchspaces.repository.WatchSpaceRepository;
import com.netflix.watchspaces.websocket.WatchSpaceRoomManager;
import com.netflix.watchspaces.websocket.WatchSpaceRoomSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TimelineServiceTest {

    @Mock
    private TimelineEventRepository timelineEventRepository;

    @Mock
    private WatchSpaceRepository watchSpaceRepository;

    @Mock
    private WatchSpaceRoomManager roomManager;

    private TimelineService timelineService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        timelineService = new TimelineService(timelineEventRepository, watchSpaceRepository, roomManager, objectMapper);
    }

    @Test
    @DisplayName("Trivia Trigger Engine: Triggers trivia event synchronously upon reaching timestamp")
    public void testCheckAndTriggerTrivia() {
        String spaceId = "ws_trivia_test";
        Long titleId = 1L;

        Title title = Title.builder().id(titleId).build();
        WatchSpace space = WatchSpace.builder().id(spaceId).title(title).build();

        WatchSpaceRoomSession session = new WatchSpaceRoomSession(spaceId, 42L, objectMapper);

        TimelineEvent triviaEvent = TimelineEvent.builder()
                .id(301L)
                .title(title)
                .tsSeconds(95)
                .eventType(EventType.TRIVIA)
                .eventTitle("VFX Breakthrough")
                .payloadJson("{\"question\": \"Software name?\", \"options\": [\"Blender\", \"Maya\"], \"answerIndex\": 0, \"durationSeconds\": 15}")
                .build();

        when(roomManager.getRoom(spaceId)).thenReturn(Optional.of(session));
        when(watchSpaceRepository.findById(spaceId)).thenReturn(Optional.of(space));
        when(timelineEventRepository.findByTitleIdAndEventType(titleId, EventType.TRIVIA))
                .thenReturn(List.of(triviaEvent));

        assertFalse(session.isTriviaTriggered(301L));

        // When playback reaches 95.5s
        StepVerifier.create(timelineService.checkAndTriggerTrivia(spaceId, 95.5))
                .verifyComplete();

        // Assert marked triggered
        assertTrue(session.isTriviaTriggered(301L));

        // Calling again at 96.0s does NOT trigger duplicate
        StepVerifier.create(timelineService.checkAndTriggerTrivia(spaceId, 96.0))
                .verifyComplete();

        assertTrue(session.isTriviaTriggered(301L));
    }
}
