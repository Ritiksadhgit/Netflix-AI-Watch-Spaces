package com.netflix.watchspaces.domain.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "watch_space_participants")
public class WatchSpaceParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "watch_space_id", nullable = false)
    private WatchSpace watchSpace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "is_muted", nullable = false)
    private boolean isMuted = false;

    @Column(name = "role_in_room", nullable = false, length = 32)
    private String roleInRoom = "PARTICIPANT";

    @CreationTimestamp
    @Column(name = "joined_at", updatable = false)
    private LocalDateTime joinedAt;

    @Column(name = "left_at")
    private LocalDateTime leftAt;

    @Column(name = "last_ping_at")
    private LocalDateTime lastPingAt = LocalDateTime.now();

    public WatchSpaceParticipant() {}

    public WatchSpaceParticipant(Long id, WatchSpace watchSpace, User user, boolean isMuted, String roleInRoom, LocalDateTime joinedAt, LocalDateTime leftAt, LocalDateTime lastPingAt) {
        this.id = id;
        this.watchSpace = watchSpace;
        this.user = user;
        this.isMuted = isMuted;
        this.roleInRoom = roleInRoom != null ? roleInRoom : "PARTICIPANT";
        this.joinedAt = joinedAt;
        this.leftAt = leftAt;
        this.lastPingAt = lastPingAt != null ? lastPingAt : LocalDateTime.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private WatchSpace watchSpace;
        private User user;
        private boolean isMuted = false;
        private String roleInRoom = "PARTICIPANT";
        private LocalDateTime joinedAt;
        private LocalDateTime leftAt;
        private LocalDateTime lastPingAt = LocalDateTime.now();

        public Builder id(Long id) { this.id = id; return this; }
        public Builder watchSpace(WatchSpace watchSpace) { this.watchSpace = watchSpace; return this; }
        public Builder user(User user) { this.user = user; return this; }
        public Builder isMuted(boolean isMuted) { this.isMuted = isMuted; return this; }
        public Builder roleInRoom(String roleInRoom) { this.roleInRoom = roleInRoom; return this; }
        public Builder joinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; return this; }
        public Builder leftAt(LocalDateTime leftAt) { this.leftAt = leftAt; return this; }
        public Builder lastPingAt(LocalDateTime lastPingAt) { this.lastPingAt = lastPingAt; return this; }

        public WatchSpaceParticipant build() {
            return new WatchSpaceParticipant(id, watchSpace, user, isMuted, roleInRoom, joinedAt, leftAt, lastPingAt);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public WatchSpace getWatchSpace() { return watchSpace; }
    public void setWatchSpace(WatchSpace watchSpace) { this.watchSpace = watchSpace; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public boolean isMuted() { return isMuted; }
    public void setMuted(boolean muted) { isMuted = muted; }

    public String getRoleInRoom() { return roleInRoom; }
    public void setRoleInRoom(String roleInRoom) { this.roleInRoom = roleInRoom; }

    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }

    public LocalDateTime getLeftAt() { return leftAt; }
    public void setLeftAt(LocalDateTime leftAt) { this.leftAt = leftAt; }

    public LocalDateTime getLastPingAt() { return lastPingAt; }
    public void setLastPingAt(LocalDateTime lastPingAt) { this.lastPingAt = lastPingAt; }
}
