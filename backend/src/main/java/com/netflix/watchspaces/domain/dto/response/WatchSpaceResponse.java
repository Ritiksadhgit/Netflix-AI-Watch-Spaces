package com.netflix.watchspaces.domain.dto.response;

import com.netflix.watchspaces.domain.entity.WatchSpace;
import com.netflix.watchspaces.domain.enums.AiVerbosity;
import com.netflix.watchspaces.domain.enums.PlaybackState;
import com.netflix.watchspaces.domain.enums.PrivacyType;
import com.netflix.watchspaces.domain.enums.WatchSpaceStatus;

import java.time.LocalDateTime;

public class WatchSpaceResponse {
    private String id;
    private String name;
    private WatchSpaceStatus status;
    private PrivacyType privacy;
    private String inviteCode;
    private Integer maxParticipants;
    private boolean isLocked;
    private AiVerbosity aiVerbosity;
    private boolean votingEnabled;
    private PlaybackState playbackState;
    private Double playbackPositionSeconds;
    private LocalDateTime playbackUpdatedAt;
    private TitleResponse title;
    private UserResponse hostUser;
    private int participantCount;
    private boolean isHost;
    private LocalDateTime createdAt;

    public WatchSpaceResponse() {}

    public WatchSpaceResponse(String id, String name, WatchSpaceStatus status, PrivacyType privacy, String inviteCode, Integer maxParticipants, boolean isLocked, AiVerbosity aiVerbosity, boolean votingEnabled, PlaybackState playbackState, Double playbackPositionSeconds, LocalDateTime playbackUpdatedAt, TitleResponse title, UserResponse hostUser, int participantCount, boolean isHost, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.privacy = privacy;
        this.inviteCode = inviteCode;
        this.maxParticipants = maxParticipants;
        this.isLocked = isLocked;
        this.aiVerbosity = aiVerbosity;
        this.votingEnabled = votingEnabled;
        this.playbackState = playbackState;
        this.playbackPositionSeconds = playbackPositionSeconds;
        this.playbackUpdatedAt = playbackUpdatedAt;
        this.title = title;
        this.hostUser = hostUser;
        this.participantCount = participantCount;
        this.isHost = isHost;
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String name;
        private WatchSpaceStatus status;
        private PrivacyType privacy;
        private String inviteCode;
        private Integer maxParticipants;
        private boolean isLocked;
        private AiVerbosity aiVerbosity;
        private boolean votingEnabled;
        private PlaybackState playbackState;
        private Double playbackPositionSeconds;
        private LocalDateTime playbackUpdatedAt;
        private TitleResponse title;
        private UserResponse hostUser;
        private int participantCount;
        private boolean isHost;
        private LocalDateTime createdAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder status(WatchSpaceStatus status) { this.status = status; return this; }
        public Builder privacy(PrivacyType privacy) { this.privacy = privacy; return this; }
        public Builder inviteCode(String inviteCode) { this.inviteCode = inviteCode; return this; }
        public Builder maxParticipants(Integer maxParticipants) { this.maxParticipants = maxParticipants; return this; }
        public Builder isLocked(boolean isLocked) { this.isLocked = isLocked; return this; }
        public Builder aiVerbosity(AiVerbosity aiVerbosity) { this.aiVerbosity = aiVerbosity; return this; }
        public Builder votingEnabled(boolean votingEnabled) { this.votingEnabled = votingEnabled; return this; }
        public Builder playbackState(PlaybackState playbackState) { this.playbackState = playbackState; return this; }
        public Builder playbackPositionSeconds(Double playbackPositionSeconds) { this.playbackPositionSeconds = playbackPositionSeconds; return this; }
        public Builder playbackUpdatedAt(LocalDateTime playbackUpdatedAt) { this.playbackUpdatedAt = playbackUpdatedAt; return this; }
        public Builder title(TitleResponse title) { this.title = title; return this; }
        public Builder hostUser(UserResponse hostUser) { this.hostUser = hostUser; return this; }
        public Builder participantCount(int participantCount) { this.participantCount = participantCount; return this; }
        public Builder isHost(boolean isHost) { this.isHost = isHost; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public WatchSpaceResponse build() {
            return new WatchSpaceResponse(id, name, status, privacy, inviteCode, maxParticipants, isLocked, aiVerbosity, votingEnabled, playbackState, playbackPositionSeconds, playbackUpdatedAt, title, hostUser, participantCount, isHost, createdAt);
        }
    }

    public static WatchSpaceResponse fromEntity(WatchSpace ws, int participantCount, Long currentUserId) {
        if (ws == null) return null;
        boolean hostMatch = currentUserId != null && ws.getHostUser() != null && currentUserId.equals(ws.getHostUser().getId());
        return WatchSpaceResponse.builder()
                .id(ws.getId())
                .name(ws.getName())
                .status(ws.getStatus())
                .privacy(ws.getPrivacy())
                .inviteCode(ws.getInviteCode())
                .maxParticipants(ws.getMaxParticipants())
                .isLocked(ws.isLocked())
                .aiVerbosity(ws.getAiVerbosity())
                .votingEnabled(ws.isVotingEnabled())
                .playbackState(ws.getPlaybackState())
                .playbackPositionSeconds(ws.getPlaybackPositionSeconds())
                .playbackUpdatedAt(ws.getPlaybackUpdatedAt())
                .title(TitleResponse.fromEntity(ws.getTitle()))
                .hostUser(UserResponse.fromEntity(ws.getHostUser()))
                .participantCount(participantCount)
                .isHost(hostMatch)
                .createdAt(ws.getCreatedAt())
                .build();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public WatchSpaceStatus getStatus() { return status; }
    public void setStatus(WatchSpaceStatus status) { this.status = status; }

    public PrivacyType getPrivacy() { return privacy; }
    public void setPrivacy(PrivacyType privacy) { this.privacy = privacy; }

    public String getInviteCode() { return inviteCode; }
    public void setInviteCode(String inviteCode) { this.inviteCode = inviteCode; }

    public Integer getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(Integer maxParticipants) { this.maxParticipants = maxParticipants; }

    public boolean isLocked() { return isLocked; }
    public void setLocked(boolean locked) { isLocked = locked; }

    public AiVerbosity getAiVerbosity() { return aiVerbosity; }
    public void setAiVerbosity(AiVerbosity aiVerbosity) { this.aiVerbosity = aiVerbosity; }

    public boolean isVotingEnabled() { return votingEnabled; }
    public void setVotingEnabled(boolean votingEnabled) { this.votingEnabled = votingEnabled; }

    public PlaybackState getPlaybackState() { return playbackState; }
    public void setPlaybackState(PlaybackState playbackState) { this.playbackState = playbackState; }

    public Double getPlaybackPositionSeconds() { return playbackPositionSeconds; }
    public void setPlaybackPositionSeconds(Double playbackPositionSeconds) { this.playbackPositionSeconds = playbackPositionSeconds; }

    public LocalDateTime getPlaybackUpdatedAt() { return playbackUpdatedAt; }
    public void setPlaybackUpdatedAt(LocalDateTime playbackUpdatedAt) { this.playbackUpdatedAt = playbackUpdatedAt; }

    public TitleResponse getTitle() { return title; }
    public void setTitle(TitleResponse title) { this.title = title; }

    public UserResponse getHostUser() { return hostUser; }
    public void setHostUser(UserResponse hostUser) { this.hostUser = hostUser; }

    public int getParticipantCount() { return participantCount; }
    public void setParticipantCount(int participantCount) { this.participantCount = participantCount; }

    public boolean isHost() { return isHost; }
    public void setHost(boolean host) { isHost = host; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
