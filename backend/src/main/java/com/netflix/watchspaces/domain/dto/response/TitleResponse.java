package com.netflix.watchspaces.domain.dto.response;

import com.netflix.watchspaces.domain.entity.Title;

public class TitleResponse {
    private Long id;
    private String name;
    private String synopsis;
    private Integer durationSeconds;
    private String videoAssetUrl;
    private String posterUrl;
    private String backdropUrl;
    private String genres;
    private String ratingCode;
    private Integer releaseYear;

    public TitleResponse() {}

    public TitleResponse(Long id, String name, String synopsis, Integer durationSeconds, String videoAssetUrl, String posterUrl, String backdropUrl, String genres, String ratingCode, Integer releaseYear) {
        this.id = id;
        this.name = name;
        this.synopsis = synopsis;
        this.durationSeconds = durationSeconds;
        this.videoAssetUrl = videoAssetUrl;
        this.posterUrl = posterUrl;
        this.backdropUrl = backdropUrl;
        this.genres = genres;
        this.ratingCode = ratingCode;
        this.releaseYear = releaseYear;
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
        private String ratingCode;
        private Integer releaseYear;

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

        public TitleResponse build() {
            return new TitleResponse(id, name, synopsis, durationSeconds, videoAssetUrl, posterUrl, backdropUrl, genres, ratingCode, releaseYear);
        }
    }

    public static TitleResponse fromEntity(Title title) {
        if (title == null) return null;
        return TitleResponse.builder()
                .id(title.getId())
                .name(title.getName())
                .synopsis(title.getSynopsis())
                .durationSeconds(title.getDurationSeconds())
                .videoAssetUrl(title.getVideoAssetUrl())
                .posterUrl(title.getPosterUrl())
                .backdropUrl(title.getBackdropUrl())
                .genres(title.getGenres())
                .ratingCode(title.getRatingCode())
                .releaseYear(title.getReleaseYear())
                .build();
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
}
