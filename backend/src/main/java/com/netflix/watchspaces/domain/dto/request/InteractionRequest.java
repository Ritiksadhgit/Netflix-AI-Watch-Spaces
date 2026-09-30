package com.netflix.watchspaces.domain.dto.request;

import jakarta.validation.constraints.NotNull;

public class InteractionRequest {

    @NotNull(message = "titleId is required")
    private Long titleId;

    private Integer watchedSeconds;

    private Boolean completed;

    private Integer rating;

    public InteractionRequest() {}

    public InteractionRequest(Long titleId, Integer watchedSeconds, Boolean completed, Integer rating) {
        this.titleId = titleId;
        this.watchedSeconds = watchedSeconds;
        this.completed = completed;
        this.rating = rating;
    }

    public Long getTitleId() { return titleId; }
    public void setTitleId(Long titleId) { this.titleId = titleId; }

    public Integer getWatchedSeconds() { return watchedSeconds; }
    public void setWatchedSeconds(Integer watchedSeconds) { this.watchedSeconds = watchedSeconds; }

    public Boolean getCompleted() { return completed; }
    public void setCompleted(Boolean completed) { this.completed = completed; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
}
