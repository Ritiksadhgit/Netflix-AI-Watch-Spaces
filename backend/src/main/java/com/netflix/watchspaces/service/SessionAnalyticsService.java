package com.netflix.watchspaces.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.watchspaces.domain.dto.response.DashboardStatsResponse;
import com.netflix.watchspaces.domain.dto.response.SessionAnalyticsResponse;
import com.netflix.watchspaces.domain.entity.Interaction;
import com.netflix.watchspaces.domain.entity.SessionAnalytics;
import com.netflix.watchspaces.domain.entity.WatchSpace;
import com.netflix.watchspaces.domain.enums.MessageType;
import com.netflix.watchspaces.repository.ChatMessageRepository;
import com.netflix.watchspaces.repository.InteractionRepository;
import com.netflix.watchspaces.repository.SessionAnalyticsRepository;
import com.netflix.watchspaces.repository.WatchSpaceRepository;
import com.netflix.watchspaces.websocket.WatchSpaceRoomManager;
import com.netflix.watchspaces.websocket.WatchSpaceRoomSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SessionAnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(SessionAnalyticsService.class);

    private final SessionAnalyticsRepository sessionAnalyticsRepository;
    private final WatchSpaceRepository watchSpaceRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final InteractionRepository interactionRepository;
    private final WatchSpaceService watchSpaceService;
    private final WatchSpaceRoomManager roomManager;
    private final ObjectMapper objectMapper;

    public SessionAnalyticsService(
            SessionAnalyticsRepository sessionAnalyticsRepository,
            WatchSpaceRepository watchSpaceRepository,
            ChatMessageRepository chatMessageRepository,
            InteractionRepository interactionRepository,
            WatchSpaceService watchSpaceService,
            WatchSpaceRoomManager roomManager,
            ObjectMapper objectMapper) {
        this.sessionAnalyticsRepository = sessionAnalyticsRepository;
        this.watchSpaceRepository = watchSpaceRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.interactionRepository = interactionRepository;
        this.watchSpaceService = watchSpaceService;
        this.roomManager = roomManager;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Mono<SessionAnalyticsResponse> reconcileAndSaveSessionAnalytics(String watchSpaceId) {
        return Mono.fromCallable(() -> {
            Optional<WatchSpace> spaceOpt = watchSpaceRepository.findById(watchSpaceId);
            if (spaceOpt.isEmpty()) {
                throw new IllegalArgumentException("Watch Space not found: " + watchSpaceId);
            }

            WatchSpace space = spaceOpt.get();
            Optional<WatchSpaceRoomSession> roomOpt = roomManager.getRoom(watchSpaceId);

            int duration = 0;
            int peak = 0;
            int aiCount = 0;
            int triviaCount = 0;
            int chatCount = 0;
            int votesCount = 0;

            if (roomOpt.isPresent()) {
                WatchSpaceRoomSession room = roomOpt.get();
                duration = room.getSessionDurationSeconds();
                peak = room.getPeakParticipants();
                aiCount = room.getAiQuestionsCount();
                triviaCount = room.getTriviaShownCount();
                chatCount = room.getChatMessagesCount();
                votesCount = room.getVotesCastCount();
            } else {
                // Count chat messages from DB if room already closed in memory
                chatCount = (int) chatMessageRepository.countByWatchSpaceId(watchSpaceId);
            }

            Map<String, Object> logMap = new HashMap<>();
            logMap.put("watchSpaceId", watchSpaceId);
            logMap.put("titleId", space.getTitle() != null ? space.getTitle().getId() : null);
            logMap.put("hostUserId", space.getHostUser() != null ? space.getHostUser().getId() : null);
            logMap.put("reconciledAt", System.currentTimeMillis());

            String logJson = "{}";
            try {
                logJson = objectMapper.writeValueAsString(logMap);
            } catch (Exception e) {
                log.warn("Failed to serialize analytics event log: {}", e.getMessage());
            }

            Optional<SessionAnalytics> existingOpt = sessionAnalyticsRepository.findByWatchSpaceId(watchSpaceId);
            SessionAnalytics analytics;

            if (existingOpt.isPresent()) {
                analytics = existingOpt.get();
                analytics.setSessionDurationSeconds(Math.max(analytics.getSessionDurationSeconds(), duration));
                analytics.setPeakParticipants(Math.max(analytics.getPeakParticipants(), peak));
                analytics.setAiQuestionsCount(Math.max(analytics.getAiQuestionsCount(), aiCount));
                analytics.setTriviaShownCount(Math.max(analytics.getTriviaShownCount(), triviaCount));
                analytics.setChatMessagesCount(Math.max(analytics.getChatMessagesCount(), chatCount));
                analytics.setVotesCastCount(Math.max(analytics.getVotesCastCount(), votesCount));
                analytics.setEventLogJson(logJson);
            } else {
                analytics = SessionAnalytics.builder()
                        .watchSpace(space)
                        .titleId(space.getTitle() != null ? space.getTitle().getId() : 0L)
                        .hostUserId(space.getHostUser() != null ? space.getHostUser().getId() : 0L)
                        .sessionDurationSeconds(duration)
                        .peakParticipants(peak)
                        .aiQuestionsCount(aiCount)
                        .triviaShownCount(triviaCount)
                        .chatMessagesCount(chatCount)
                        .votesCastCount(votesCount)
                        .eventLogJson(logJson)
                        .build();
            }

            SessionAnalytics saved = sessionAnalyticsRepository.save(analytics);
            log.info("Persisted session analytics for watch space {}: duration={}s, peak={}, msgs={}, ai={}",
                    watchSpaceId, saved.getSessionDurationSeconds(), saved.getPeakParticipants(), saved.getChatMessagesCount(), saved.getAiQuestionsCount());

            String titleName = space.getTitle() != null ? space.getTitle().getName() : "";
            return SessionAnalyticsResponse.fromEntity(saved, titleName);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<SessionAnalyticsResponse> getSessionAnalytics(String watchSpaceId) {
        return Mono.fromCallable(() -> {
            Optional<SessionAnalytics> dbOpt = sessionAnalyticsRepository.findByWatchSpaceId(watchSpaceId);
            if (dbOpt.isPresent()) {
                SessionAnalytics entity = dbOpt.get();
                String titleName = entity.getWatchSpace() != null && entity.getWatchSpace().getTitle() != null
                        ? entity.getWatchSpace().getTitle().getName()
                        : "Watch Space Stream";
                return SessionAnalyticsResponse.fromEntity(entity, titleName);
            }

            // If not yet persisted, check active live room
            Optional<WatchSpaceRoomSession> roomOpt = roomManager.getRoom(watchSpaceId);
            Optional<WatchSpace> spaceOpt = watchSpaceRepository.findById(watchSpaceId);

            if (spaceOpt.isPresent()) {
                WatchSpace space = spaceOpt.get();
                int duration = roomOpt.map(WatchSpaceRoomSession::getSessionDurationSeconds).orElse(0);
                int peak = roomOpt.map(WatchSpaceRoomSession::getPeakParticipants).orElse(1);
                int ai = roomOpt.map(WatchSpaceRoomSession::getAiQuestionsCount).orElse(0);
                int trivia = roomOpt.map(WatchSpaceRoomSession::getTriviaShownCount).orElse(0);
                int chat = roomOpt.map(WatchSpaceRoomSession::getChatMessagesCount)
                        .orElse((int) chatMessageRepository.countByWatchSpaceId(watchSpaceId));
                int votes = roomOpt.map(WatchSpaceRoomSession::getVotesCastCount).orElse(0);

                String titleName = space.getTitle() != null ? space.getTitle().getName() : "Watch Space Stream";
                return new SessionAnalyticsResponse(
                        null,
                        watchSpaceId,
                        space.getTitle() != null ? space.getTitle().getId() : null,
                        titleName,
                        space.getHostUser() != null ? space.getHostUser().getId() : null,
                        duration,
                        peak,
                        ai,
                        trivia,
                        chat,
                        votes,
                        ""
                );
            }

            throw new IllegalArgumentException("Session analytics not found for space: " + watchSpaceId);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<DashboardStatsResponse> getDashboardStats() {
        return watchSpaceService.getActivePublicSpaces(null)
                .collectList()
                .publishOn(Schedulers.boundedElastic())
                .map(activeSpaces -> {
                    int liveSpacesCount = activeSpaces.size();

                    // Calculate total watched seconds from session analytics & interactions
                    List<SessionAnalytics> analyticsList = sessionAnalyticsRepository.findAll();
                    long sessionDurationSum = analyticsList.stream()
                            .mapToLong(SessionAnalytics::getSessionDurationSeconds)
                            .sum();

                    List<Interaction> interactions = interactionRepository.findAll();
                    long interactionDurationSum = interactions.stream()
                            .mapToLong(i -> i.getWatchedSeconds() != null ? i.getWatchedSeconds() : 0)
                            .sum();

                    long totalWatchedSeconds = sessionDurationSum + interactionDurationSum;
                    double totalWatchedHours = Math.round((totalWatchedSeconds / 3600.0) * 10.0) / 10.0;

                    // Calculate AI questions count from session analytics and chat messages
                    int analyticsAiQuestions = analyticsList.stream()
                            .mapToInt(SessionAnalytics::getAiQuestionsCount)
                            .sum();
                    long chatAiMessages = chatMessageRepository.countByMsgType(MessageType.AI);
                    int totalAiQuestions = (int) (analyticsAiQuestions + chatAiMessages);

                    // Trivia accuracy: null when unmeasured, rendered as "—"
                    Double triviaAccuracy = null;

                    return new DashboardStatsResponse(
                            liveSpacesCount,
                            totalWatchedSeconds,
                            totalWatchedHours,
                            totalAiQuestions,
                            triviaAccuracy
                    );
                });
    }
}
