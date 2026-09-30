package com.netflix.watchspaces.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.watchspaces.domain.dto.response.AiAnswerResponse;
import com.netflix.watchspaces.domain.entity.TimelineEvent;
import com.netflix.watchspaces.domain.entity.Title;
import com.netflix.watchspaces.domain.enums.EventType;
import com.netflix.watchspaces.repository.TimelineEventRepository;
import com.netflix.watchspaces.repository.TitleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AiGroundingServiceTest {

    @Mock
    private TimelineEventRepository timelineEventRepository;

    @Mock
    private TitleRepository titleRepository;

    private AiGroundingService aiGroundingService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Long titleId = 1L;

    @BeforeEach
    public void setUp() {
        aiGroundingService = new AiGroundingService(timelineEventRepository, titleRepository, objectMapper);
    }

    @Test
    @DisplayName("AI Co-Pilot answers character inquiry with verified timelineEventId citations")
    public void testAiAnswerIncludesTimelineEventIdCitations() {
        Title title = Title.builder().id(titleId).name("Tears of Steel").build();

        TimelineEvent sceneEvent = TimelineEvent.builder()
                .id(1L)
                .title(title)
                .tsSeconds(15)
                .eventType(EventType.SCENE)
                .eventTitle("The Desolate Bridge of Amsterdam")
                .payloadJson("{\"description\": \"Opening scene on the bridge.\"}")
                .build();

        TimelineEvent charEvent = TimelineEvent.builder()
                .id(2L)
                .title(title)
                .tsSeconds(45)
                .eventType(EventType.CHARACTER)
                .eventTitle("Thom (The Lead Pilot)")
                .payloadJson("{\"name\": \"Thom\", \"role\": \"Lead Pilot / Roboticist\", \"description\": \"Thom leads the resistance.\"}")
                .build();

        // Range [45 - 90, 45 + 30] -> [0, 75]
        when(timelineEventRepository.findByTitleIdAndTsRange(eq(titleId), eq(0), eq(75)))
                .thenReturn(List.of(sceneEvent, charEvent));

        StepVerifier.create(aiGroundingService.answerQuestion(titleId, "Who is Thom?", 45.0, "NORMAL"))
                .assertNext((AiAnswerResponse res) -> {
                    assertNotNull(res.getAnswer());
                    assertTrue(res.getAnswer().contains("Thom"));
                    assertTrue(res.getAnswer().contains("Lead Pilot"));

                    // Verify citations
                    assertFalse(res.getSources().isEmpty());
                    assertEquals(2L, res.getSources().get(0).getTimelineEventId());
                    assertEquals("CHARACTER", res.getSources().get(0).getEventType());
                    assertEquals("Thom (The Lead Pilot)", res.getSources().get(0).getTitle());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("AI Co-Pilot applies strict anti-hallucination contract when topic is absent")
    public void testAiUngroundedFallback() {
        Title title = Title.builder().id(titleId).name("Tears of Steel").build();

        TimelineEvent sceneEvent = TimelineEvent.builder()
                .id(1L)
                .title(title)
                .tsSeconds(15)
                .eventType(EventType.SCENE)
                .eventTitle("The Desolate Bridge of Amsterdam")
                .payloadJson("{\"description\": \"Opening scene on the bridge under dark skies.\"}")
                .build();

        when(timelineEventRepository.findByTitleIdAndTsRange(eq(titleId), eq(0), eq(50)))
                .thenReturn(List.of(sceneEvent));

        StepVerifier.create(aiGroundingService.answerQuestion(titleId, "What is the stock price of Apple?", 20.0, "NORMAL"))
                .assertNext((AiAnswerResponse res) -> {
                    assertNotNull(res.getAnswer());
                    // Assert anti-hallucination fallback format
                    assertTrue(res.getAnswer().contains("Based on the authored timeline for this title, details regarding"));
                    assertTrue(res.getAnswer().contains("are not documented"));
                    assertTrue(res.getAnswer().contains("The Desolate Bridge of Amsterdam"));

                    // Cites the active scene
                    assertFalse(res.getSources().isEmpty());
                    assertEquals(1L, res.getSources().get(0).getTimelineEventId());
                })
                .verifyComplete();
    }
}
