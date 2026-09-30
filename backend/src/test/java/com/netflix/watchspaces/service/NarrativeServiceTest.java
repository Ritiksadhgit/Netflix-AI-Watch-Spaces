package com.netflix.watchspaces.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.watchspaces.domain.dto.response.VariationAppliedResponse;
import com.netflix.watchspaces.domain.entity.TimelineEvent;
import com.netflix.watchspaces.domain.entity.Title;
import com.netflix.watchspaces.domain.entity.VariationOption;
import com.netflix.watchspaces.domain.entity.WatchSpace;
import com.netflix.watchspaces.domain.enums.EventType;
import com.netflix.watchspaces.repository.TimelineEventRepository;
import com.netflix.watchspaces.repository.VariationOptionRepository;
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
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NarrativeServiceTest {

    @Mock
    private TimelineEventRepository timelineEventRepository;

    @Mock
    private VariationOptionRepository variationOptionRepository;

    @Mock
    private WatchSpaceRepository watchSpaceRepository;

    @Mock
    private WatchSpaceRoomManager roomManager;

    private NarrativeService narrativeService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        narrativeService = new NarrativeService(
                timelineEventRepository,
                variationOptionRepository,
                watchSpaceRepository,
                roomManager,
                objectMapper
        );
    }

    @Test
    @DisplayName("Narrative Voting: Triggers voting session and tallies cast votes")
    public void testNarrativeVotingFlow() {
        String spaceId = "ws_narrative_test";
        Long titleId = 1L;
        Long variationEventId = 5L;

        Title title = Title.builder().id(titleId).build();
        WatchSpace space = WatchSpace.builder().id(spaceId).title(title).build();
        WatchSpaceRoomSession session = new WatchSpaceRoomSession(spaceId, 42L, objectMapper);

        TimelineEvent variationEvent = TimelineEvent.builder()
                .id(variationEventId)
                .title(title)
                .tsSeconds(240)
                .eventType(EventType.VARIATION)
                .eventTitle("Tactical Decision Point")
                .payloadJson("{\"prompt\": \"Select tactic\", \"durationSeconds\": 20}")
                .build();

        VariationOption opt1 = VariationOption.builder().id(101L).timelineEvent(variationEvent).label("Option A").assetRef("branch_a").voteCount(0).build();
        VariationOption opt2 = VariationOption.builder().id(102L).timelineEvent(variationEvent).label("Option B").assetRef("branch_b").voteCount(0).build();

        when(roomManager.getRoom(spaceId)).thenReturn(Optional.of(session));
        when(watchSpaceRepository.findById(spaceId)).thenReturn(Optional.of(space));
        when(timelineEventRepository.findByTitleIdAndEventType(titleId, EventType.VARIATION))
                .thenReturn(List.of(variationEvent));
        when(variationOptionRepository.findByTimelineEventId(variationEventId))
                .thenReturn(List.of(opt1, opt2));
        when(variationOptionRepository.findById(102L)).thenReturn(Optional.of(opt2));

        // 1. Trigger voting at 240.5s
        StepVerifier.create(narrativeService.checkAndTriggerVariation(spaceId, 240.5))
                .verifyComplete();

        assertNotNull(session.getActiveVotingSession());

        // 2. Cast votes
        StepVerifier.create(narrativeService.castVote(spaceId, 1L, variationEventId, 102L))
                .assertNext((Map<String, Object> tally) -> {
                    assertEquals(1, tally.get("totalVotes"));
                })
                .verifyComplete();

        StepVerifier.create(narrativeService.castVote(spaceId, 2L, variationEventId, 102L))
                .assertNext((Map<String, Object> tally) -> {
                    assertEquals(2, tally.get("totalVotes"));
                })
                .verifyComplete();

        // 3. Finalize voting
        StepVerifier.create(narrativeService.finalizeVote(spaceId, variationEventId))
                .assertNext((VariationAppliedResponse res) -> {
                    assertEquals(102L, res.getWinningOptionId());
                    assertEquals("Option B", res.getWinningLabel());
                    assertEquals(2, res.getTotalVotes());
                    assertEquals("branch_b", res.getAssetRef());
                })
                .verifyComplete();
    }
}
