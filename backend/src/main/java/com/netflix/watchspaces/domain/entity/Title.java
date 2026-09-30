package com.netflix.watchspaces.domain.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "titles")
public class Title {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String synopsis;

    @Column(name = "duration_seconds", nullable = false)
    private Integer durationSeconds;

    @Column(name = "video_asset_url", nullable = false, length = 1024)
    private String videoAssetUrl;

    @Column(name = "poster_url", length = 1024)
    private String posterUrl;

    @Column(name = "backdrop_url", length = 1024)
    private String backdropUrl;

    @Column(nullable = false)
    private String genres;

    @Column(name = "rating_code", length = 16)
    private String ratingCode = "PG-13";

    @Column(name = "release_year")
    private Integer releaseYear = 2024;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Title() {}

    public Title(Long id, String name, String synopsis, Integer durationSeconds, String videoAssetUrl, String posterUrl, String backdropUrl, String genres, String ratingCode, Integer releaseYear, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.synopsis = synopsis;
        this.durationSeconds = durationSeconds;
        this.videoAssetUrl = videoAssetUrl;
        this.posterUrl = posterUrl;
        this.backdropUrl = backdropUrl;
        this.genres = genres;
        this.ratingCode = ratingCode != null ? ratingCode : "PG-13";
        this.releaseYear = releaseYear != null ? releaseYear : 2024;
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String name;
        private String synopsis;
        private Integer durationSeconds;
        private String videoAssetUrl;
        private String posterUrl;
        private String backdropUrl;
        private String genres;
        private String ratingCode = "PG-13";
        private Integer releaseYear = 2024;
        private LocalDateTime createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder synopsis(String synopsis) { this.synopsis = synopsis; return this; }
        public Builder durationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; return this; }
        public Builder videoAssetUrl(String videoAssetUrl) { this.videoAssetUrl = videoAssetUrl; return this; }
        public Builder posterUrl(String posterUrl) { this.posterUrl = posterUrl; return this; }
        public Builder backdropUrl(String backdropUrl) { this.backdropUrl = backdropUrl; return this; }
        public Builder genres(String genres) { this.genres = genres; return this; }
        public Builder ratingCode(String ratingCode) { this.ratingCode = ratingCode; return this; }
        public Builder releaseYear(Integer releaseYear) { this.releaseYear = releaseYear; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public Title build() {
            return new Title(id, name, synopsis, durationSeconds, videoAssetUrl, posterUrl, backdropUrl, genres, ratingCode, releaseYear, createdAt);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSynopsis() { return synopsis; }
    public void setSynopsis(String synopsis) { this.synopsis = synopsis; }

    public Integer getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }

    public String getVideoAssetUrl() { return videoAssetUrl; }
    public void setVideoAssetUrl(String videoAssetUrl) { this.videoAssetUrl = videoAssetUrl; }

    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

    public String getBackdropUrl() { return backdropUrl; }
    public void setBackdropUrl(String backdropUrl) { this.backdropUrl = backdropUrl; }

    public String getGenres() { return genres; }
    public void setGenres(String genres) { this.genres = genres; }

    public String getRatingCode() { return ratingCode; }
    public void setRatingCode(String ratingCode) { this.ratingCode = ratingCode; }

    public Integer getReleaseYear() { return releaseYear; }
    public void setReleaseYear(Integer releaseYear) { this.releaseYear = releaseYear; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
