package com.netflix.watchspaces.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.watchspaces.domain.dto.response.SessionAnalyticsResponse;
import com.netflix.watchspaces.domain.entity.SessionAnalytics;
import com.netflix.watchspaces.domain.entity.Title;
import com.netflix.watchspaces.domain.entity.User;
import com.netflix.watchspaces.domain.entity.WatchSpace;
import com.netflix.watchspaces.repository.ChatMessageRepository;
import com.netflix.watchspaces.repository.SessionAnalyticsRepository;
import com.netflix.watchspaces.repository.WatchSpaceRepository;
import com.netflix.watchspaces.websocket.WatchSpaceRoomManager;
import com.netflix.watchspaces.websocket.WatchSpaceRoomSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SessionAnalyticsServiceTest {

    @Mock
    private SessionAnalyticsRepository sessionAnalyticsRepository;

    @Mock
    private WatchSpaceRepository watchSpaceRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private WatchSpaceRoomManager roomManager;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private SessionAnalyticsService sessionAnalyticsService;

    private WatchSpace testSpace;
    private WatchSpaceRoomSession testRoom;

    @BeforeEach
    void setUp() {
        Title title = Title.builder().id(1L).name("Tears of Steel").build();
        User host = User.builder().id(2L).displayName("Host User").build();
        testSpace = WatchSpace.builder()
                .id("ws_analytics_test")
                .title(title)
                .hostUser(host)
                .build();

        testRoom = new WatchSpaceRoomSession("ws_analytics_test", 2L, objectMapper);
        testRoom.addParticipant("sess_1", 2L, "Host", "avatar.jpg", "HOST");
        testRoom.addParticipant("sess_2", 3L, "Viewer", "avatar2.jpg", "PARTICIPANT");
        testRoom.incrementChatMessagesCount();
        testRoom.incrementChatMessagesCount();
        testRoom.incrementAiQuestionsCount();
        testRoom.incrementTriviaShownCount();
    }

    @Test
    void testReconcileAndSaveSessionAnalytics() {
        when(watchSpaceRepository.findById("ws_analytics_test")).thenReturn(Optional.of(testSpace));
        when(roomManager.getRoom("ws_analytics_test")).thenReturn(Optional.of(testRoom));
        when(sessionAnalyticsRepository.findByWatchSpaceId("ws_analytics_test")).thenReturn(Optional.empty());

        when(sessionAnalyticsRepository.save(any(SessionAnalytics.class))).thenAnswer(invocation -> {
            SessionAnalytics saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        StepVerifier.create(sessionAnalyticsService.reconcileAndSaveSessionAnalytics("ws_analytics_test"))
                .assertNext(res -> {
                    assertNotNull(res);
                    assertEquals("ws_analytics_test", res.getWatchSpaceId());
                    assertEquals("Tears of Steel", res.getTitleName());
                    assertEquals(2, res.getPeakParticipants());
                    assertEquals(2, res.getChatMessagesCount());
                    assertEquals(1, res.getAiQuestionsCount());
                    assertEquals(1, res.getTriviaShownCount());
                })
                .verifyComplete();
    }

    @Test
    void testGetSessionAnalyticsFromDb() {
        SessionAnalytics entity = SessionAnalytics.builder()
                .id(1L)
                .watchSpace(testSpace)
                .titleId(1L)
                .hostUserId(2L)
                .sessionDurationSeconds(350)
                .peakParticipants(4)
                .chatMessagesCount(15)
                .aiQuestionsCount(3)
                .triviaShownCount(2)
                .votesCastCount(5)
                .build();

        when(sessionAnalyticsRepository.findByWatchSpaceId("ws_analytics_test")).thenReturn(Optional.of(entity));

        StepVerifier.create(sessionAnalyticsService.getSessionAnalytics("ws_analytics_test"))
                .assertNext(res -> {
                    assertEquals("ws_analytics_test", res.getWatchSpaceId());
                    assertEquals(4, res.getPeakParticipants());
                    assertEquals(15, res.getChatMessagesCount());
                })
                .verifyComplete();
    }
}
