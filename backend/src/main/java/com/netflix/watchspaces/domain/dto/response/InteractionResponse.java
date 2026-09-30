package com.netflix.watchspaces.domain.dto.response;

import com.netflix.watchspaces.domain.entity.Interaction;

import java.time.format.DateTimeFormatter;

public class InteractionResponse {

    private Long id;
    private Long userId;
    private Long titleId;
    private String titleName;
    private String posterUrl;
    private String backdropUrl;
    private Integer watchedSeconds;
    private Integer durationSeconds;
    private Integer progressPercent;
    private Boolean completed;
    private Integer rating;
    private String updatedAt;

    public InteractionResponse() {}

    public InteractionResponse(Long id, Long userId, Long titleId, String titleName, String posterUrl, String backdropUrl, Integer watchedSeconds, Integer durationSeconds, Integer progressPercent, Boolean completed, Integer rating, String updatedAt) {
        this.id = id;
        this.userId = userId;
        this.titleId = titleId;
        this.titleName = titleName;
        this.posterUrl = posterUrl;
        this.backdropUrl = backdropUrl;
        this.watchedSeconds = watchedSeconds;
        this.durationSeconds = durationSeconds;
        this.progressPercent = progressPercent;
        this.completed = completed;
        this.rating = rating;
        this.updatedAt = updatedAt;
    }

    public static InteractionResponse fromEntity(Interaction interaction) {
        if (interaction == null) return null;

        String name = interaction.getTitle() != null ? interaction.getTitle().getName() : "";
        String poster = interaction.getTitle() != null ? interaction.getTitle().getPosterUrl() : "";
        String backdrop = interaction.getTitle() != null ? interaction.getTitle().getBackdropUrl() : "";
        int duration = interaction.getTitle() != null ? interaction.getTitle().getDurationSeconds() : 0;
        int watched = interaction.getWatchedSeconds() != null ? interaction.getWatchedSeconds() : 0;
        int progress = duration > 0 ? Math.min(100, (int) Math.round(((double) watched / duration) * 100)) : 0;

        String updatedStr = interaction.getUpdatedAt() != null
                ? interaction.getUpdatedAt().format(DateTimeFormatter.ISO_DATE_TIME)
                : "";

        return new InteractionResponse(
                interaction.getId(),
                interaction.getUser() != null ? interaction.getUser().getId() : null,
                interaction.getTitle() != null ? interaction.getTitle().getId() : null,
                name,
                poster,
                backdrop,
                watched,
                duration,
                progress,
                interaction.isCompleted(),
                interaction.getRating(),
                updatedStr
        );
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getTitleId() { return titleId; }
    public void setTitleId(Long titleId) { this.titleId = titleId; }

    public String getTitleName() { return titleName; }
    public void setTitleName(String titleName) { this.titleName = titleName; }

    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

    public String getBackdropUrl() { return backdropUrl; }
    public void setBackdropUrl(String backdropUrl) { this.backdropUrl = backdropUrl; }

    public Integer getWatchedSeconds() { return watchedSeconds; }
    public void setWatchedSeconds(Integer watchedSeconds) { this.watchedSeconds = watchedSeconds; }

    public Integer getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }

    public Integer getProgressPercent() { return progressPercent; }
    public void setProgressPercent(Integer progressPercent) { this.progressPercent = progressPercent; }

    public Boolean getCompleted() { return completed; }
    public void setCompleted(Boolean completed) { this.completed = completed; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
