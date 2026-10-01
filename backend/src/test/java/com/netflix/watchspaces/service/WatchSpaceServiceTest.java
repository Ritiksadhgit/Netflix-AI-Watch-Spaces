package com.netflix.watchspaces.service;

import com.netflix.watchspaces.domain.dto.request.CreateWatchSpaceRequest;
import com.netflix.watchspaces.domain.dto.response.WatchSpaceResponse;
import com.netflix.watchspaces.domain.entity.Title;
import com.netflix.watchspaces.domain.entity.User;
import com.netflix.watchspaces.domain.entity.WatchSpace;
import com.netflix.watchspaces.domain.entity.WatchSpaceParticipant;
import com.netflix.watchspaces.domain.enums.AiVerbosity;
import com.netflix.watchspaces.domain.enums.PlaybackState;
import com.netflix.watchspaces.domain.enums.PrivacyType;
import com.netflix.watchspaces.domain.enums.RoleType;
import com.netflix.watchspaces.domain.enums.WatchSpaceStatus;
import com.netflix.watchspaces.repository.TitleRepository;
import com.netflix.watchspaces.repository.UserRepository;
import com.netflix.watchspaces.repository.WatchSpaceParticipantRepository;
import com.netflix.watchspaces.repository.WatchSpaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WatchSpaceServiceTest {

    @Mock
    private WatchSpaceRepository watchSpaceRepository;

    @Mock
    private WatchSpaceParticipantRepository participantRepository;

    @Mock
    private TitleRepository titleRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private WatchSpaceService watchSpaceService;

    private User hostUser;
    private Title title;

    @BeforeEach
    void setUp() {
        hostUser = User.builder()
                .id(10L)
                .email("host@example.com")
                .displayName("Host User")
                .role(RoleType.HOST)
                .build();

        title = Title.builder()
                .id(1L)
                .name("Tears of Steel")
                .durationSeconds(734)
                .videoAssetUrl("https://example.com/video.mp4")
                .build();
    }

    @Test
    @DisplayName("createWatchSpace - Should successfully create space and construct response with loaded entities")
    void testCreateWatchSpace() {
        CreateWatchSpaceRequest request = CreateWatchSpaceRequest.builder()
                .titleId(1L)
                .name("My Watch Space")
                .privacy(PrivacyType.PUBLIC)
                .maxParticipants(20)
                .aiVerbosity(AiVerbosity.NORMAL)
                .votingEnabled(true)
                .build();

        when(userRepository.findById(10L)).thenReturn(Optional.of(hostUser));
        when(titleRepository.findById(1L)).thenReturn(Optional.of(title));

        when(watchSpaceRepository.save(any(WatchSpace.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(participantRepository.save(any(WatchSpaceParticipant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StepVerifier.create(watchSpaceService.createWatchSpace(request, 10L))
                .assertNext(response -> {
                    assertNotNull(response);
                    assertTrue(response.getId().startsWith("ws_"));
                    assertEquals("My Watch Space", response.getName());
                    assertEquals(WatchSpaceStatus.LIVE, response.getStatus());
                    assertEquals(PrivacyType.PUBLIC, response.getPrivacy());
                    assertNotNull(response.getTitle());
                    assertEquals(1L, response.getTitle().getId());
                    assertEquals("Tears of Steel", response.getTitle().getName());
                    assertNotNull(response.getHostUser());
                    assertEquals(10L, response.getHostUser().getId());
                    assertEquals("Host User", response.getHostUser().getDisplayName());
                    assertTrue(response.isHost());
                    assertEquals(1, response.getParticipantCount());
                })
                .verifyComplete();

        verify(watchSpaceRepository).save(any(WatchSpace.class));
        verify(participantRepository).save(any(WatchSpaceParticipant.class));
    }

    @Test
    @DisplayName("getWatchSpaceById - Should retrieve space and map active participant count")
    void testGetWatchSpaceById() {
        WatchSpace space = WatchSpace.builder()
                .id("ws_test_1")
                .name("Existing Space")
                .title(title)
                .hostUser(hostUser)
                .status(WatchSpaceStatus.LIVE)
                .privacy(PrivacyType.PUBLIC)
                .playbackState(PlaybackState.PAUSED)
                .playbackPositionSeconds(0.0)
                .playbackUpdatedAt(LocalDateTime.now())
                .build();

        when(watchSpaceRepository.findById("ws_test_1")).thenReturn(Optional.of(space));
        when(participantRepository.findActiveByWatchSpaceId("ws_test_1")).thenReturn(Collections.emptyList());

        StepVerifier.create(watchSpaceService.getWatchSpaceById("ws_test_1", 10L))
                .assertNext(res -> {
                    assertEquals("ws_test_1", res.getId());
                    assertEquals("Existing Space", res.getName());
                    assertTrue(res.isHost());
                })
                .verifyComplete();
    }
}
