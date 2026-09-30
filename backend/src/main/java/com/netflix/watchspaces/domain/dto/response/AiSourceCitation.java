package com.netflix.watchspaces.domain.dto.response;

public class AiSourceCitation {

    private Long timelineEventId;
    private Integer timestamp;
    private String eventType;
    private String title;
    private String snippet;

    public AiSourceCitation() {}

    public AiSourceCitation(Long timelineEventId, Integer timestamp, String eventType, String title, String snippet) {
        this.timelineEventId = timelineEventId;
        this.timestamp = timestamp;
        this.eventType = eventType;
        this.title = title;
        this.snippet = snippet;
    }

    public Long getTimelineEventId() { return timelineEventId; }
    public void setTimelineEventId(Long timelineEventId) { this.timelineEventId = timelineEventId; }

    public Integer getTimestamp() { return timestamp; }
    public void setTimestamp(Integer timestamp) { this.timestamp = timestamp; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSnippet() { return snippet; }
    public void setSnippet(String snippet) { this.snippet = snippet; }
}
