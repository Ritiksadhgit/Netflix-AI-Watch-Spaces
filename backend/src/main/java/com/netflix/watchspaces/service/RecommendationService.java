package com.netflix.watchspaces.service;

import com.netflix.watchspaces.domain.dto.response.RecommendationResponse;
import com.netflix.watchspaces.domain.entity.Interaction;
import com.netflix.watchspaces.domain.entity.Title;
import com.netflix.watchspaces.repository.InteractionRepository;
import com.netflix.watchspaces.repository.TitleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);

    private final TitleRepository titleRepository;
    private final InteractionRepository interactionRepository;

    public RecommendationService(TitleRepository titleRepository, InteractionRepository interactionRepository) {
        this.titleRepository = titleRepository;
        this.interactionRepository = interactionRepository;
    }

    public Mono<List<RecommendationResponse>> getPersonalizedRecommendations(Long userId) {
        return Mono.fromCallable(() -> {
            List<Title> allTitles = titleRepository.findAll();
            if (allTitles.isEmpty()) return Collections.<RecommendationResponse>emptyList();

            List<Interaction> userInteractions = userId != null
                    ? interactionRepository.findByUserId(userId)
                    : Collections.emptyList();

            // Extract genre affinities and watched titles
            Map<String, Integer> genreAffinity = new HashMap<>();
            Set<Long> watchedTitleIds = new HashSet<>();
            String lastWatchedTitleName = null;

            for (Interaction interaction : userInteractions) {
                if (interaction.getTitle() != null) {
                    watchedTitleIds.add(interaction.getTitle().getId());
                    lastWatchedTitleName = interaction.getTitle().getName();
                    String genres = interaction.getTitle().getGenres();
                    if (genres != null) {
                        for (String g : genres.split(",")) {
                            String trimmed = g.trim().toLowerCase();
                            genreAffinity.put(trimmed, genreAffinity.getOrDefault(trimmed, 0) + 1);
                        }
                    }
                }
            }

            // Find top preferred genre
            String topGenre = genreAffinity.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);

            List<RecommendationResponse> recommendations = new ArrayList<>();

            for (Title title : allTitles) {
                int score = 75; // Baseline score
                String reason;
                String category = "TRENDING";

                // Check genre overlap
                String genres = title.getGenres() != null ? title.getGenres().toLowerCase() : "";
                boolean genreMatched = false;
                if (topGenre != null && genres.contains(topGenre)) {
                    score += 16;
                    genreMatched = true;
                }

                // If watched before
                if (watchedTitleIds.contains(title.getId())) {
                    score = Math.min(99, score + 8);
                    reason = "Rewatch your favorite";
                    category = "TOP_MATCH";
                } else if (genreMatched && lastWatchedTitleName != null) {
                    score = Math.min(98, score + 7);
                    reason = "Because you watched " + lastWatchedTitleName;
                    category = "TOP_MATCH";
                } else if (title.getReleaseYear() != null && title.getReleaseYear() >= 2023) {
                    score = Math.min(95, score + 12);
                    reason = "Top Trending in " + (title.getGenres() != null ? title.getGenres().split(",")[0].trim() : "Catalog");
                    category = "TRENDING";
                } else {
                    reason = "Critically Acclaimed Community Choice";
                    category = "COMMUNITY";
                }

                score = Math.max(70, Math.min(99, score));

                recommendations.add(new RecommendationResponse(
                        title.getId(),
                        title.getName(),
                        title.getSynopsis(),
                        title.getDurationSeconds(),
                        title.getVideoAssetUrl(),
                        title.getPosterUrl(),
                        title.getBackdropUrl(),
                        title.getGenres(),
                        title.getRatingCode(),
                        title.getReleaseYear(),
                        score,
                        reason,
                        category
                ));
            }

            // Sort by matchScore descending
            recommendations.sort(Comparator.comparingInt(RecommendationResponse::getMatchScore).reversed());
            return recommendations;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Map<String, List<RecommendationResponse>>> getCategorizedRecommendations(Long userId) {
        return getPersonalizedRecommendations(userId).map(list -> {
            Map<String, List<RecommendationResponse>> rails = new LinkedHashMap<>();

            // 1. Top Picks for You (Match Score >= 85)
            List<RecommendationResponse> topPicks = list.stream()
                    .filter(r -> r.getMatchScore() >= 85)
                    .collect(Collectors.toList());
            if (!topPicks.isEmpty()) {
                rails.put("Top Picks for You", topPicks);
            }

            // 2. Sci-Fi & Cyberpunk
            List<RecommendationResponse> sciFi = list.stream()
                    .filter(r -> r.getGenres() != null && (r.getGenres().toLowerCase().contains("sci-fi") || r.getGenres().toLowerCase().contains("cyberpunk")))
                    .collect(Collectors.toList());
            if (!sciFi.isEmpty()) {
                rails.put("Sci-Fi & Cyberpunk", sciFi);
            }

            // 3. Fantasy, Animation & Family
            List<RecommendationResponse> animation = list.stream()
                    .filter(r -> r.getGenres() != null && (r.getGenres().toLowerCase().contains("animation") || r.getGenres().toLowerCase().contains("fantasy")))
                    .collect(Collectors.toList());
            if (!animation.isEmpty()) {
                rails.put("Animation & Fantasy Quests", animation);
            }

            // 4. All Featured Titles
            rails.put("Explore All Watch Titles", list);

            return rails;
        });
    }
}
