package com.netflix.watchspaces.domain.dto.response;

import com.netflix.watchspaces.domain.entity.SessionAnalytics;

import java.time.format.DateTimeFormatter;

public class SessionAnalyticsResponse {

    private Long id;
    private String watchSpaceId;
    private Long titleId;
    private String titleName;
    private Long hostUserId;
    private Integer sessionDurationSeconds;
    private Integer peakParticipants;
    private Integer aiQuestionsCount;
    private Integer triviaShownCount;
    private Integer chatMessagesCount;
    private Integer votesCastCount;
    private String createdAt;

    public SessionAnalyticsResponse() {}

    public SessionAnalyticsResponse(Long id, String watchSpaceId, Long titleId, String titleName, Long hostUserId, Integer sessionDurationSeconds, Integer peakParticipants, Integer aiQuestionsCount, Integer triviaShownCount, Integer chatMessagesCount, Integer votesCastCount, String createdAt) {
        this.id = id;
        this.watchSpaceId = watchSpaceId;
        this.titleId = titleId;
        this.titleName = titleName;
        this.hostUserId = hostUserId;
        this.sessionDurationSeconds = sessionDurationSeconds;
        this.peakParticipants = peakParticipants;
        this.aiQuestionsCount = aiQuestionsCount;
        this.triviaShownCount = triviaShownCount;
        this.chatMessagesCount = chatMessagesCount;
        this.votesCastCount = votesCastCount;
        this.createdAt = createdAt;
    }

    public static SessionAnalyticsResponse fromEntity(SessionAnalytics entity, String titleName) {
        if (entity == null) return null;

        String createdStr = entity.getCreatedAt() != null
                ? entity.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME)
                : "";

        String spaceId = entity.getWatchSpace() != null ? entity.getWatchSpace().getId() : "";

        return new SessionAnalyticsResponse(
                entity.getId(),
                spaceId,
                entity.getTitleId(),
                titleName,
                entity.getHostUserId(),
                entity.getSessionDurationSeconds(),
                entity.getPeakParticipants(),
                entity.getAiQuestionsCount(),
                entity.getTriviaShownCount(),
                entity.getChatMessagesCount(),
                entity.getVotesCastCount(),
                createdStr
        );
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getWatchSpaceId() { return watchSpaceId; }
    public void setWatchSpaceId(String watchSpaceId) { this.watchSpaceId = watchSpaceId; }

    public Long getTitleId() { return titleId; }
    public void setTitleId(Long titleId) { this.titleId = titleId; }

    public String getTitleName() { return titleName; }
    public void setTitleName(String titleName) { this.titleName = titleName; }

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

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
