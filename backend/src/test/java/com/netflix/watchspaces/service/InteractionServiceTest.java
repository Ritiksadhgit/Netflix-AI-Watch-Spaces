package com.netflix.watchspaces.service;

import com.netflix.watchspaces.domain.dto.request.InteractionRequest;
import com.netflix.watchspaces.domain.dto.response.InteractionResponse;
import com.netflix.watchspaces.domain.entity.Interaction;
import com.netflix.watchspaces.domain.entity.Title;
import com.netflix.watchspaces.domain.entity.User;
import com.netflix.watchspaces.repository.InteractionRepository;
import com.netflix.watchspaces.repository.TitleRepository;
import com.netflix.watchspaces.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InteractionServiceTest {

    @Mock
    private InteractionRepository interactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TitleRepository titleRepository;

    @InjectMocks
    private InteractionService interactionService;

    private User testUser;
    private Title testTitle;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(5L).email("viewer@test.com").displayName("Viewer").build();
        testTitle = Title.builder().id(1L).name("Tears of Steel").durationSeconds(734).build();
    }

    @Test
    void testRecordInteractionNewRecord() {
        InteractionRequest req = new InteractionRequest(1L, 400, false, 5);

        when(userRepository.findById(5L)).thenReturn(Optional.of(testUser));
        when(titleRepository.findById(1L)).thenReturn(Optional.of(testTitle));
        when(interactionRepository.findByUserIdAndTitleId(5L, 1L)).thenReturn(Optional.empty());

        when(interactionRepository.save(any(Interaction.class))).thenAnswer(invocation -> {
            Interaction saved = invocation.getArgument(0);
            saved.setId(99L);
            return saved;
        });

        StepVerifier.create(interactionService.recordInteraction(5L, req))
                .assertNext(res -> {
                    assertNotNull(res);
                    assertEquals(5L, res.getUserId());
                    assertEquals(1L, res.getTitleId());
                    assertEquals(400, res.getWatchedSeconds());
                    assertEquals(5, res.getRating());
                    assertFalse(res.getCompleted());
                })
                .verifyComplete();
    }

    @Test
    void testRecordInteractionCompletionAutoCalculated() {
        // 700 seconds out of 734 seconds is > 85%, should auto-complete
        InteractionRequest req = new InteractionRequest(1L, 700, null, 4);

        when(userRepository.findById(5L)).thenReturn(Optional.of(testUser));
        when(titleRepository.findById(1L)).thenReturn(Optional.of(testTitle));
        when(interactionRepository.findByUserIdAndTitleId(5L, 1L)).thenReturn(Optional.empty());

        when(interactionRepository.save(any(Interaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StepVerifier.create(interactionService.recordInteraction(5L, req))
                .assertNext(res -> {
                    assertTrue(res.getCompleted());
                    assertEquals(700, res.getWatchedSeconds());
                })
                .verifyComplete();
    }

    @Test
    void testGetUserWatchHistory() {
        Interaction interaction = Interaction.builder()
                .id(1L)
                .user(testUser)
                .title(testTitle)
                .watchedSeconds(350)
                .completed(false)
                .build();

        when(interactionRepository.findByUserId(5L)).thenReturn(List.of(interaction));

        StepVerifier.create(interactionService.getUserWatchHistory(5L))
                .assertNext(history -> {
                    assertEquals(1, history.size());
                    assertEquals("Tears of Steel", history.get(0).getTitleName());
                })
                .verifyComplete();
    }
}
