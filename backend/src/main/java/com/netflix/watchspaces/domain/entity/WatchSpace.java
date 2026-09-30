package com.netflix.watchspaces.domain.entity;

import com.netflix.watchspaces.domain.enums.AiVerbosity;
import com.netflix.watchspaces.domain.enums.PlaybackState;
import com.netflix.watchspaces.domain.enums.PrivacyType;
import com.netflix.watchspaces.domain.enums.WatchSpaceStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "watch_spaces")
public class WatchSpace {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "title_id", nullable = false)
    private Title title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_user_id", nullable = false)
    private User hostUser;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private WatchSpaceStatus status = WatchSpaceStatus.LIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PrivacyType privacy = PrivacyType.PUBLIC;

    @Column(name = "invite_code", nullable = false, unique = true, length = 64)
    private String inviteCode;

    @Column(name = "max_participants", nullable = false)
    private Integer maxParticipants = 50;

    @Column(name = "is_locked", nullable = false)
    private boolean isLocked = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "ai_verbosity", nullable = false, length = 32)
    private AiVerbosity aiVerbosity = AiVerbosity.NORMAL;

    @Column(name = "voting_enabled", nullable = false)
    private boolean votingEnabled = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "playback_state", nullable = false, length = 32)
    private PlaybackState playbackState = PlaybackState.PAUSED;

    @Column(name = "playback_position_seconds", nullable = false)
    private Double playbackPositionSeconds = 0.0;

    @Column(name = "playback_updated_at")
    private LocalDateTime playbackUpdatedAt = LocalDateTime.now();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    public WatchSpace() {}

    public WatchSpace(String id, Title title, User hostUser, String name, WatchSpaceStatus status, PrivacyType privacy, String inviteCode, Integer maxParticipants, boolean isLocked, AiVerbosity aiVerbosity, boolean votingEnabled, PlaybackState playbackState, Double playbackPositionSeconds, LocalDateTime playbackUpdatedAt, LocalDateTime createdAt, LocalDateTime endedAt) {
        this.id = id;
        this.title = title;
        this.hostUser = hostUser;
        this.name = name;
        this.status = status != null ? status : WatchSpaceStatus.LIVE;
        this.privacy = privacy != null ? privacy : PrivacyType.PUBLIC;
        this.inviteCode = inviteCode;
        this.maxParticipants = maxParticipants != null ? maxParticipants : 50;
        this.isLocked = isLocked;
        this.aiVerbosity = aiVerbosity != null ? aiVerbosity : AiVerbosity.NORMAL;
        this.votingEnabled = votingEnabled;
        this.playbackState = playbackState != null ? playbackState : PlaybackState.PAUSED;
        this.playbackPositionSeconds = playbackPositionSeconds != null ? playbackPositionSeconds : 0.0;
        this.playbackUpdatedAt = playbackUpdatedAt != null ? playbackUpdatedAt : LocalDateTime.now();
        this.createdAt = createdAt;
        this.endedAt = endedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private Title title;
        private User hostUser;
        private String name;
        private WatchSpaceStatus status = WatchSpaceStatus.LIVE;
        private PrivacyType privacy = PrivacyType.PUBLIC;
        private String inviteCode;
        private Integer maxParticipants = 50;
        private boolean isLocked = false;
        private AiVerbosity aiVerbosity = AiVerbosity.NORMAL;
        private boolean votingEnabled = true;
        private PlaybackState playbackState = PlaybackState.PAUSED;
        private Double playbackPositionSeconds = 0.0;
        private LocalDateTime playbackUpdatedAt = LocalDateTime.now();
        private LocalDateTime createdAt;
        private LocalDateTime endedAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder title(Title title) { this.title = title; return this; }
        public Builder hostUser(User hostUser) { this.hostUser = hostUser; return this; }
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
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder endedAt(LocalDateTime endedAt) { this.endedAt = endedAt; return this; }

        public WatchSpace build() {
            return new WatchSpace(id, title, hostUser, name, status, privacy, inviteCode, maxParticipants, isLocked, aiVerbosity, votingEnabled, playbackState, playbackPositionSeconds, playbackUpdatedAt, createdAt, endedAt);
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Title getTitle() { return title; }
    public void setTitle(Title title) { this.title = title; }

    public User getHostUser() { return hostUser; }
    public void setHostUser(User hostUser) { this.hostUser = hostUser; }

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getEndedAt() { return endedAt; }
    public void setEndedAt(LocalDateTime endedAt) { this.endedAt = endedAt; }
}
