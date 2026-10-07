package com.netflix.watchspaces.domain.dto.response;

public class DashboardStatsResponse {

    private int liveSpacesCount;
    private long totalWatchedSeconds;
    private double totalWatchedHours;
    private int aiQuestionsCount;
    private Double triviaAccuracy; // null when unmeasured to avoid inventing fake statistics

    public DashboardStatsResponse() {}

    public DashboardStatsResponse(int liveSpacesCount, long totalWatchedSeconds, double totalWatchedHours, int aiQuestionsCount, Double triviaAccuracy) {
        this.liveSpacesCount = liveSpacesCount;
        this.totalWatchedSeconds = totalWatchedSeconds;
        this.totalWatchedHours = totalWatchedHours;
        this.aiQuestionsCount = aiQuestionsCount;
        this.triviaAccuracy = triviaAccuracy;
    }

    public int getLiveSpacesCount() { return liveSpacesCount; }
    public void setLiveSpacesCount(int liveSpacesCount) { this.liveSpacesCount = liveSpacesCount; }

    public long getTotalWatchedSeconds() { return totalWatchedSeconds; }
    public void setTotalWatchedSeconds(long totalWatchedSeconds) { this.totalWatchedSeconds = totalWatchedSeconds; }

    public double getTotalWatchedHours() { return totalWatchedHours; }
    public void setTotalWatchedHours(double totalWatchedHours) { this.totalWatchedHours = totalWatchedHours; }

    public int getAiQuestionsCount() { return aiQuestionsCount; }
    public void setAiQuestionsCount(int aiQuestionsCount) { this.aiQuestionsCount = aiQuestionsCount; }

    public Double getTriviaAccuracy() { return triviaAccuracy; }
    public void setTriviaAccuracy(Double triviaAccuracy) { this.triviaAccuracy = triviaAccuracy; }
}
