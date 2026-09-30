package com.netflix.watchspaces.domain.dto.response;

import com.netflix.watchspaces.domain.entity.User;
import com.netflix.watchspaces.domain.enums.RoleType;

import java.time.LocalDateTime;

public class UserResponse {
    private Long id;
    private String email;
    private String displayName;
    private RoleType role;
    private String subtitleLocale;
    private String avatarUrl;
    private LocalDateTime createdAt;

    public UserResponse() {}

    public UserResponse(Long id, String email, String displayName, RoleType role, String subtitleLocale, String avatarUrl, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.displayName = displayName;
        this.role = role;
        this.subtitleLocale = subtitleLocale;
        this.avatarUrl = avatarUrl;
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String email;
        private String displayName;
        private RoleType role;
        private String subtitleLocale;
        private String avatarUrl;
        private LocalDateTime createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder displayName(String displayName) { this.displayName = displayName; return this; }
        public Builder role(RoleType role) { this.role = role; return this; }
        public Builder subtitleLocale(String subtitleLocale) { this.subtitleLocale = subtitleLocale; return this; }
        public Builder avatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public UserResponse build() {
            return new UserResponse(id, email, displayName, role, subtitleLocale, avatarUrl, createdAt);
        }
    }

    public static UserResponse fromEntity(User user) {
        if (user == null) return null;
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .role(user.getRole())
                .subtitleLocale(user.getSubtitleLocale())
                .avatarUrl(user.getAvatarUrl())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

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
}
