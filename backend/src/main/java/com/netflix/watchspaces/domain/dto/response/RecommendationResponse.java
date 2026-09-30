package com.netflix.watchspaces.domain.dto.response;

public class RecommendationResponse {

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
    private Integer matchScore;
    private String matchReason;
    private String category;

    public RecommendationResponse() {}

    public RecommendationResponse(Long id, String name, String synopsis, Integer durationSeconds, String videoAssetUrl, String posterUrl, String backdropUrl, String genres, String ratingCode, Integer releaseYear, Integer matchScore, String matchReason, String category) {
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
        this.matchScore = matchScore;
        this.matchReason = matchReason;
        this.category = category;
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

    public Integer getMatchScore() { return matchScore; }
    public void setMatchScore(Integer matchScore) { this.matchScore = matchScore; }

    public String getMatchReason() { return matchReason; }
    public void setMatchReason(String matchReason) { this.matchReason = matchReason; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
