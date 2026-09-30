package com.netflix.watchspaces.domain.entity;

import com.netflix.watchspaces.domain.enums.RoleType;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 191)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private RoleType role = RoleType.VIEWER;

    @Column(name = "subtitle_locale", nullable = false, length = 10)
    private String subtitleLocale = "en-US";

    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public User() {}

    public User(Long id, String email, String passwordHash, String displayName, RoleType role, String subtitleLocale, String avatarUrl, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.role = role != null ? role : RoleType.VIEWER;
        this.subtitleLocale = subtitleLocale != null ? subtitleLocale : "en-US";
        this.avatarUrl = avatarUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String email;
        private String passwordHash;
        private String displayName;
        private RoleType role = RoleType.VIEWER;
        private String subtitleLocale = "en-US";
        private String avatarUrl;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder passwordHash(String passwordHash) { this.passwordHash = passwordHash; return this; }
        public Builder displayName(String displayName) { this.displayName = displayName; return this; }
        public Builder role(RoleType role) { this.role = role; return this; }
        public Builder subtitleLocale(String subtitleLocale) { this.subtitleLocale = subtitleLocale; return this; }
        public Builder avatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public User build() {
            return new User(id, email, passwordHash, displayName, role, subtitleLocale, avatarUrl, createdAt, updatedAt);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public RoleType getRole() { return role; }
    public void setRole(RoleType role) { this.role = role; }

    public String getSubtitleLocale() { return subtitleLocale; }
    public void setSubtitleLocale(String subtitleLocale) { this.subtitleLocale = subtitleLocale; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
