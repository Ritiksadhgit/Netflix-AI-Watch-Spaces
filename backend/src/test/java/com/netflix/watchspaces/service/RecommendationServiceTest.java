package com.netflix.watchspaces.service;

import com.netflix.watchspaces.domain.dto.response.RecommendationResponse;
import com.netflix.watchspaces.domain.entity.Interaction;
import com.netflix.watchspaces.domain.entity.Title;
import com.netflix.watchspaces.domain.entity.User;
import com.netflix.watchspaces.repository.InteractionRepository;
import com.netflix.watchspaces.repository.TitleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RecommendationServiceTest {

    @Mock
    private TitleRepository titleRepository;

    @Mock
    private InteractionRepository interactionRepository;

    @InjectMocks
    private RecommendationService recommendationService;

    private Title titleSciFi;
    private Title titleAnimation;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(10L).email("user@test.com").displayName("Test User").build();

        titleSciFi = Title.builder()
                .id(1L)
                .name("Tears of Steel")
                .synopsis("Sci-Fi action")
                .genres("Sci-Fi, Cyberpunk")
                .durationSeconds(734)
                .releaseYear(2024)
                .build();

        titleAnimation = Title.builder()
                .id(2L)
                .name("Big Buck Bunny")
                .synopsis("Funny woodland comedy")
                .genres("Animation, Comedy")
                .durationSeconds(596)
                .releaseYear(2022)
                .build();
    }

    @Test
    void testPersonalizedRecommendationsBoostsMatchingGenres() {
        Interaction interaction = Interaction.builder()
                .id(100L)
                .user(testUser)
                .title(titleSciFi)
                .watchedSeconds(500)
                .completed(true)
                .build();

        when(interactionRepository.findByUserId(10L)).thenReturn(List.of(interaction));
        when(titleRepository.findAll()).thenReturn(List.of(titleSciFi, titleAnimation));

        StepVerifier.create(recommendationService.getPersonalizedRecommendations(10L))
                .assertNext(recommendations -> {
                    assertEquals(2, recommendations.size());
                    RecommendationResponse first = recommendations.get(0);
                    // Sci-Fi title should rank first because user interacted with Sci-Fi
                    assertEquals(1L, first.getId());
                    assertTrue(first.getMatchScore() >= 85);
                })
                .verifyComplete();
    }

    @Test
    void testCategorizedRecommendationsProducesRails() {
        when(interactionRepository.findByUserId(10L)).thenReturn(List.of());
        when(titleRepository.findAll()).thenReturn(List.of(titleSciFi, titleAnimation));

        StepVerifier.create(recommendationService.getCategorizedRecommendations(10L))
                .assertNext(rails -> {
                    assertNotNull(rails);
                    assertTrue(rails.containsKey("Explore All Watch Titles"));
                    assertTrue(rails.containsKey("Sci-Fi & Cyberpunk"));
                    assertTrue(rails.containsKey("Animation & Fantasy Quests"));
                })
                .verifyComplete();
    }
}
