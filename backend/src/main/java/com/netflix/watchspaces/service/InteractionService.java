package com.netflix.watchspaces.service;

import com.netflix.watchspaces.domain.dto.request.InteractionRequest;
import com.netflix.watchspaces.domain.dto.response.InteractionResponse;
import com.netflix.watchspaces.domain.entity.Interaction;
import com.netflix.watchspaces.domain.entity.Title;
import com.netflix.watchspaces.domain.entity.User;
import com.netflix.watchspaces.repository.InteractionRepository;
import com.netflix.watchspaces.repository.TitleRepository;
import com.netflix.watchspaces.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InteractionService {

    private static final Logger log = LoggerFactory.getLogger(InteractionService.class);

    private final InteractionRepository interactionRepository;
    private final UserRepository userRepository;
    private final TitleRepository titleRepository;

    public InteractionService(InteractionRepository interactionRepository, UserRepository userRepository, TitleRepository titleRepository) {
        this.interactionRepository = interactionRepository;
        this.userRepository = userRepository;
        this.titleRepository = titleRepository;
    }

    @Transactional
    public Mono<InteractionResponse> recordInteraction(Long userId, InteractionRequest request) {
        return Mono.fromCallable(() -> {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                throw new IllegalArgumentException("User not found: " + userId);
            }

            Optional<Title> titleOpt = titleRepository.findById(request.getTitleId());
            if (titleOpt.isEmpty()) {
                throw new IllegalArgumentException("Title not found: " + request.getTitleId());
            }

            User user = userOpt.get();
            Title title = titleOpt.get();

            Optional<Interaction> existingOpt = interactionRepository.findByUserIdAndTitleId(userId, request.getTitleId());
            Interaction interaction;

            if (existingOpt.isPresent()) {
                interaction = existingOpt.get();
                if (request.getWatchedSeconds() != null) {
                    interaction.setWatchedSeconds(Math.max(interaction.getWatchedSeconds(), request.getWatchedSeconds()));
                }
            } else {
                interaction = Interaction.builder()
                        .user(user)
                        .title(title)
                        .watchedSeconds(request.getWatchedSeconds() != null ? request.getWatchedSeconds() : 0)
                        .completed(false)
                        .build();
            }

            // Determine completion
            if (Boolean.TRUE.equals(request.getCompleted())) {
                interaction.setCompleted(true);
            } else if (title.getDurationSeconds() != null && title.getDurationSeconds() > 0) {
                if (interaction.getWatchedSeconds() >= (title.getDurationSeconds() * 0.85)) {
                    interaction.setCompleted(true);
                }
            }

            if (request.getRating() != null) {
                int clamped = Math.max(1, Math.min(5, request.getRating()));
                interaction.setRating(clamped);
            }

            Interaction saved = interactionRepository.save(interaction);
            log.info("Recorded interaction for user {} on title {}: {}s, completed={}", userId, title.getId(), saved.getWatchedSeconds(), saved.isCompleted());
            return InteractionResponse.fromEntity(saved);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<List<InteractionResponse>> getUserWatchHistory(Long userId) {
        return Mono.fromCallable(() -> {
            List<Interaction> interactions = interactionRepository.findByUserId(userId);
            return interactions.stream()
                    .sorted((a, b) -> {
                        if (a.getUpdatedAt() == null && b.getUpdatedAt() == null) return 0;
                        if (a.getUpdatedAt() == null) return 1;
                        if (b.getUpdatedAt() == null) return -1;
                        return b.getUpdatedAt().compareTo(a.getUpdatedAt());
                    })
                    .map(InteractionResponse::fromEntity)
                    .collect(Collectors.toList());
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
