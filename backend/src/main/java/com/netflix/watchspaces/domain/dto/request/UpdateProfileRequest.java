package com.netflix.watchspaces.domain.dto.request;

import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {

    @Size(min = 2, max = 50, message = "Display name must be between 2 and 50 characters")
    private String displayName;

    private String avatarUrl;

    @Size(max = 10, message = "Subtitle locale must be at most 10 characters")
    private String subtitleLocale;

    public UpdateProfileRequest() {}

    public UpdateProfileRequest(String displayName, String avatarUrl, String subtitleLocale) {
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
        this.subtitleLocale = subtitleLocale;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getSubtitleLocale() {
        return subtitleLocale;
    }

    public void setSubtitleLocale(String subtitleLocale) {
        this.subtitleLocale = subtitleLocale;
    }
}
