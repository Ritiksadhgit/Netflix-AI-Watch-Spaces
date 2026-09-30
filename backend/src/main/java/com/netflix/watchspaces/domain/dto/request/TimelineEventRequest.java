package com.netflix.watchspaces.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public class TimelineEventRequest {

    @NotNull(message = "tsSeconds is required")
    @PositiveOrZero(message = "tsSeconds must be non-negative")
    private Integer tsSeconds;

    @NotBlank(message = "eventType is required")
    private String eventType;

    @NotBlank(message = "title is required")
    @Size(min = 1, max = 255, message = "title must be between 1 and 255 characters")
    private String title;

    @NotBlank(message = "payloadJson is required")
    private String payloadJson;

    public TimelineEventRequest() {}

    public TimelineEventRequest(Integer tsSeconds, String eventType, String title, String payloadJson) {
        this.tsSeconds = tsSeconds;
        this.eventType = eventType;
        this.title = title;
        this.payloadJson = payloadJson;
    }

    public Integer getTsSeconds() { return tsSeconds; }
    public void setTsSeconds(Integer tsSeconds) { this.tsSeconds = tsSeconds; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }
}
