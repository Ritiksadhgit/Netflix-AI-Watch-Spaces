package com.netflix.watchspaces.domain.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "session_analytics")
public class SessionAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "watch_space_id", nullable = false, unique = true)
    private WatchSpace watchSpace;

    @Column(name = "title_id", nullable = false)
    private Long titleId;

    @Column(name = "host_user_id", nullable = false)
    private Long hostUserId;

    @Column(name = "session_duration_seconds", nullable = false)
    private Integer sessionDurationSeconds = 0;

    @Column(name = "peak_participants", nullable = false)
    private Integer peakParticipants = 0;

    @Column(name = "ai_questions_count", nullable = false)
    private Integer aiQuestionsCount = 0;

    @Column(name = "trivia_shown_count", nullable = false)
    private Integer triviaShownCount = 0;

    @Column(name = "chat_messages_count", nullable = false)
    private Integer chatMessagesCount = 0;

    @Column(name = "votes_cast_count", nullable = false)
    private Integer votesCastCount = 0;

    @Column(name = "event_log", nullable = false, columnDefinition = "JSON")
    private String eventLogJson;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public SessionAnalytics() {}

    public SessionAnalytics(Long id, WatchSpace watchSpace, Long titleId, Long hostUserId, Integer sessionDurationSeconds, Integer peakParticipants, Integer aiQuestionsCount, Integer triviaShownCount, Integer chatMessagesCount, Integer votesCastCount, String eventLogJson, LocalDateTime createdAt) {
        this.id = id;
        this.watchSpace = watchSpace;
        this.titleId = titleId;
        this.hostUserId = hostUserId;
        this.sessionDurationSeconds = sessionDurationSeconds != null ? sessionDurationSeconds : 0;
        this.peakParticipants = peakParticipants != null ? peakParticipants : 0;
        this.aiQuestionsCount = aiQuestionsCount != null ? aiQuestionsCount : 0;
        this.triviaShownCount = triviaShownCount != null ? triviaShownCount : 0;
        this.chatMessagesCount = chatMessagesCount != null ? chatMessagesCount : 0;
        this.votesCastCount = votesCastCount != null ? votesCastCount : 0;
        this.eventLogJson = eventLogJson;
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private WatchSpace watchSpace;
        private Long titleId;
        private Long hostUserId;
        private Integer sessionDurationSeconds = 0;
        private Integer peakParticipants = 0;
        private Integer aiQuestionsCount = 0;
        private Integer triviaShownCount = 0;
        private Integer chatMessagesCount = 0;
        private Integer votesCastCount = 0;
        private String eventLogJson;
        private LocalDateTime createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder watchSpace(WatchSpace watchSpace) { this.watchSpace = watchSpace; return this; }
        public Builder titleId(Long titleId) { this.titleId = titleId; return this; }
        public Builder hostUserId(Long hostUserId) { this.hostUserId = hostUserId; return this; }
        public Builder sessionDurationSeconds(Integer sessionDurationSeconds) { this.sessionDurationSeconds = sessionDurationSeconds; return this; }
        public Builder peakParticipants(Integer peakParticipants) { this.peakParticipants = peakParticipants; return this; }
        public Builder aiQuestionsCount(Integer aiQuestionsCount) { this.aiQuestionsCount = aiQuestionsCount; return this; }
        public Builder triviaShownCount(Integer triviaShownCount) { this.triviaShownCount = triviaShownCount; return this; }
        public Builder chatMessagesCount(Integer chatMessagesCount) { this.chatMessagesCount = chatMessagesCount; return this; }
        public Builder votesCastCount(Integer votesCastCount) { this.votesCastCount = votesCastCount; return this; }
        public Builder eventLogJson(String eventLogJson) { this.eventLogJson = eventLogJson; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public SessionAnalytics build() {
            return new SessionAnalytics(id, watchSpace, titleId, hostUserId, sessionDurationSeconds, peakParticipants, aiQuestionsCount, triviaShownCount, chatMessagesCount, votesCastCount, eventLogJson, createdAt);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public WatchSpace getWatchSpace() { return watchSpace; }
    public void setWatchSpace(WatchSpace watchSpace) { this.watchSpace = watchSpace; }

    public Long getTitleId() { return titleId; }
    public void setTitleId(Long titleId) { this.titleId = titleId; }

    public Long getHostUserId() { return hostUserId; }
    public void setHostUserId(Long hostUserId) { this.hostUserId = hostUserId; }

    public Integer getSessionDurationSeconds() { return sessionDurationSeconds; }
    public void setSessionDurationSeconds(Integer sessionDurationSeconds) { this.sessionDurationSeconds = sessionDurationSeconds; }

    public Integer getPeakParticipants() { return peakParticipants; }
    public void setPeakParticipants(Integer peakParticipants) { this.peakParticipants = peakParticipants; }

    public Integer getAiQuestionsCount() { return aiQuestionsCount; }
    public void setAiQuestionsCount(Integer aiQuestionsCount) { this.aiQuestionsCount = aiQuestionsCount; }

    public Integer getTriviaShownCount() { return triviaShownCount; }
    public void setTriviaShownCount(Integer triviaShownCount) { this.triviaShownCount = triviaShownCount; }

    public Integer getChatMessagesCount() { return chatMessagesCount; }
    public void setChatMessagesCount(Integer chatMessagesCount) { this.chatMessagesCount = chatMessagesCount; }

    public Integer getVotesCastCount() { return votesCastCount; }
    public void setVotesCastCount(Integer votesCastCount) { this.votesCastCount = votesCastCount; }

    public String getEventLogJson() { return eventLogLog(); }
    public String eventLogLog() { return eventLogJson; }
    public void setEventLogJson(String eventLogJson) { this.eventLogJson = eventLogJson; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
