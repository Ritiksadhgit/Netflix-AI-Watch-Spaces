package com.netflix.watchspaces.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.watchspaces.domain.dto.request.TimelineEventRequest;
import com.netflix.watchspaces.domain.dto.request.TimelineImportRequest;
import com.netflix.watchspaces.domain.dto.response.TimelineImportResultResponse;
import com.netflix.watchspaces.domain.dto.response.TimelineResponse;
import com.netflix.watchspaces.domain.entity.TimelineEvent;
import com.netflix.watchspaces.domain.entity.Title;
import com.netflix.watchspaces.domain.enums.EventType;
import com.netflix.watchspaces.repository.TimelineEventRepository;
import com.netflix.watchspaces.repository.TitleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminTimelineServiceTest {

    @Mock
    private TimelineEventRepository timelineEventRepository;

    @Mock
    private TitleRepository titleRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private AdminTimelineService adminTimelineService;

    private Title testTitle;

    @BeforeEach
    void setUp() {
        testTitle = Title.builder()
                .id(1L)
                .name("Tears of Steel")
                .durationSeconds(734)
                .build();
    }

    @Test
    void testCreateMarkerSuccess() {
        TimelineEventRequest req = new TimelineEventRequest(
                45,
                "SCENE",
                "Thom's Robotic Arm Activation",
                "{\"sceneNumber\": 2, \"character\": \"Thom\"}"
        );

        when(titleRepository.findById(1L)).thenReturn(Optional.of(testTitle));
        when(timelineEventRepository.save(any(TimelineEvent.class))).thenAnswer(inv -> {
            TimelineEvent ev = inv.getArgument(0);
            ev.setId(101L);
            return ev;
        });

        StepVerifier.create(adminTimelineService.createMarker(1L, req))
                .assertNext(res -> {
                    assertNotNull(res);
                    assertEquals(101L, res.getId());
                    assertEquals(45, res.getTsSeconds());
                    assertEquals("SCENE", res.getEventType());
                    assertEquals("Thom's Robotic Arm Activation", res.getTitle());
                })
                .verifyComplete();
    }

    @Test
    void testMalformedTimelineJsonRejectedWith422() {
        TimelineEventRequest req = new TimelineEventRequest(
                50,
                "TRIVIA",
                "Trivia Title",
                "{malformed_json_without_quotes: true" // Bad JSON syntax
        );

        when(titleRepository.findById(1L)).thenReturn(Optional.of(testTitle));

        StepVerifier.create(adminTimelineService.createMarker(1L, req))
                .expectErrorMatches(throwable -> {
                    assertTrue(throwable instanceof ResponseStatusException);
                    ResponseStatusException ex = (ResponseStatusException) throwable;
                    return ex.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY &&
                            ex.getReason() != null && ex.getReason().contains("malformed JSON syntax");
                })
                .verify();

        verify(timelineEventRepository, never()).save(any());
    }

    @Test
    void testTimestampExceedingDurationRejectedWith422() {
        TimelineEventRequest req = new TimelineEventRequest(
                9999, // Exceeds title duration (734s)
                "SCENE",
                "Out of bounds event",
                "{\"scene\": 99}"
        );

        when(titleRepository.findById(1L)).thenReturn(Optional.of(testTitle));

        StepVerifier.create(adminTimelineService.createMarker(1L, req))
                .expectErrorMatches(throwable -> {
                    assertTrue(throwable instanceof ResponseStatusException);
                    ResponseStatusException ex = (ResponseStatusException) throwable;
                    return ex.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY &&
                            ex.getReason() != null && ex.getReason().contains("exceeds title duration");
                })
                .verify();
    }

    @Test
    void testNegativeTimestampRejectedWith422() {
        TimelineEventRequest req = new TimelineEventRequest(
                -10,
                "SCENE",
                "Negative timestamp event",
                "{\"scene\": -1}"
        );

        when(titleRepository.findById(1L)).thenReturn(Optional.of(testTitle));

        StepVerifier.create(adminTimelineService.createMarker(1L, req))
                .expectErrorMatches(throwable -> {
                    assertTrue(throwable instanceof ResponseStatusException);
                    ResponseStatusException ex = (ResponseStatusException) throwable;
                    return ex.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY &&
                            ex.getReason() != null && ex.getReason().contains("must be non-negative");
                })
                .verify();
    }

    @Test
    void testInvalidEventTypeRejectedWith422() {
        TimelineEventRequest req = new TimelineEventRequest(
                120,
                "INVALID_TYPE_XYZ",
                "Invalid Event",
                "{\"data\": 123}"
        );

        when(titleRepository.findById(1L)).thenReturn(Optional.of(testTitle));

        StepVerifier.create(adminTimelineService.createMarker(1L, req))
                .expectErrorMatches(throwable -> {
                    assertTrue(throwable instanceof ResponseStatusException);
                    ResponseStatusException ex = (ResponseStatusException) throwable;
                    return ex.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY &&
                            ex.getReason() != null && ex.getReason().contains("invalid eventType");
                })
                .verify();
    }

    @Test
    void testAtomicImportRollbackOnSingleError() {
        TimelineEventRequest valid1 = new TimelineEventRequest(10, "SCENE", "Scene 1", "{\"ok\": 1}");
        TimelineEventRequest invalid2 = new TimelineEventRequest(20, "TRIVIA", "Trivia 1", "{bad_json: 123");
        TimelineEventRequest valid3 = new TimelineEventRequest(30, "CHARACTER", "Char 1", "{\"ok\": 3}");

        TimelineImportRequest importReq = new TimelineImportRequest(List.of(valid1, invalid2, valid3), true);

        when(titleRepository.findById(1L)).thenReturn(Optional.of(testTitle));

        StepVerifier.create(adminTimelineService.importTimeline(1L, importReq))
                .expectErrorMatches(throwable -> {
                    assertTrue(throwable instanceof ResponseStatusException);
                    ResponseStatusException ex = (ResponseStatusException) throwable;
                    return ex.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY &&
                            ex.getReason() != null && ex.getReason().contains("Event #2");
                })
                .verify();

        // Verify that NO entities were deleted or saved because atomic validation failed prior to persistence
        verify(timelineEventRepository, never()).deleteAll(any());
        verify(timelineEventRepository, never()).saveAll(any());
    }

    @Test
    void testValidTimelineJsonPersistedAtomically() {
        TimelineEventRequest valid1 = new TimelineEventRequest(15, "SCENE", "Bridge Scene", "{\"scene\": 1}");
        TimelineEventRequest valid2 = new TimelineEventRequest(95, "TRIVIA", "VFX Trivia", "{\"question\": \"Test?\"}");

        TimelineImportRequest importReq = new TimelineImportRequest(List.of(valid1, valid2), false);

        when(titleRepository.findById(1L)).thenReturn(Optional.of(testTitle));
        when(timelineEventRepository.saveAll(any())).thenAnswer(inv -> {
            List<TimelineEvent> list = inv.getArgument(0);
            return list;
        });

        StepVerifier.create(adminTimelineService.importTimeline(1L, importReq))
                .assertNext(res -> {
                    assertNotNull(res);
                    assertEquals(2, res.getImportedCount());
                    assertEquals("SUCCESS", res.getStatus());
                })
                .verifyComplete();
    }

    @Test
    void testUpdateAndDeleteMarker() {
        TimelineEvent existing = TimelineEvent.builder()
                .id(55L)
                .title(testTitle)
                .tsSeconds(100)
                .eventType(EventType.SCENE)
                .eventTitle("Old Title")
                .payloadJson("{\"old\": true}")
                .build();

        when(titleRepository.findById(1L)).thenReturn(Optional.of(testTitle));
        when(timelineEventRepository.findById(55L)).thenReturn(Optional.of(existing));
        when(timelineEventRepository.save(any(TimelineEvent.class))).thenAnswer(inv -> inv.getArgument(0));

        TimelineEventRequest updateReq = new TimelineEventRequest(
                120,
                "GLOSSARY",
                "New Title",
                "{\"updated\": true}"
        );

        StepVerifier.create(adminTimelineService.updateMarker(1L, 55L, updateReq))
                .assertNext(updated -> {
                    assertEquals(120, updated.getTsSeconds());
                    assertEquals("GLOSSARY", updated.getEventType());
                    assertEquals("New Title", updated.getTitle());
                })
                .verifyComplete();

        // Delete test
        StepVerifier.create(adminTimelineService.deleteMarker(1L, 55L))
                .verifyComplete();

        verify(timelineEventRepository, times(1)).delete(existing);
    }
}
