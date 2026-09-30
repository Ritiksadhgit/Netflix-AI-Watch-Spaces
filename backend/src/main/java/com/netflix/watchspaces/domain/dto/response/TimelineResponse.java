package com.netflix.watchspaces.domain.dto.response;

import com.netflix.watchspaces.domain.entity.TimelineEvent;

public class TimelineResponse {

    private Long id;
    private Long titleId;
    private Integer tsSeconds;
    private String eventType;
    private String title;
    private String payloadJson;

    public TimelineResponse() {}

    public TimelineResponse(Long id, Long titleId, Integer tsSeconds, String eventType, String title, String payloadJson) {
        this.id = id;
        this.titleId = titleId;
        this.tsSeconds = tsSeconds;
        this.eventType = eventType;
        this.title = title;
        this.payloadJson = payloadJson;
    }

    public static TimelineResponse fromEntity(TimelineEvent event) {
        return new TimelineResponse(
                event.getId(),
                event.getTitle() != null ? event.getTitle().getId() : null,
                event.getTsSeconds(),
                event.getEventType() != null ? event.getEventType().name() : "SCENE",
                event.getEventTitle(),
                event.getPayloadJson()
        );
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTitleId() { return titleId; }
    public void setTitleId(Long titleId) { this.titleId = titleId; }

    public Integer getTsSeconds() { return tsSeconds; }
    public void setTsSeconds(Integer tsSeconds) { this.tsSeconds = tsSeconds; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }
}
