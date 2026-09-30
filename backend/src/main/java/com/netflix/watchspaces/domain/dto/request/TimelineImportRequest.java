package com.netflix.watchspaces.domain.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class TimelineImportRequest {

    private boolean replaceExisting = false;

    @NotEmpty(message = "events list cannot be empty")
    @Valid
    private List<TimelineEventRequest> events;

    public TimelineImportRequest() {}

    public TimelineImportRequest(List<TimelineEventRequest> events, boolean replaceExisting) {
        this.events = events;
        this.replaceExisting = replaceExisting;
    }

    public boolean isReplaceExisting() { return replaceExisting; }
    public void setReplaceExisting(boolean replaceExisting) { this.replaceExisting = replaceExisting; }

    public List<TimelineEventRequest> getEvents() { return events; }
    public void setEvents(List<TimelineEventRequest> events) { this.events = events; }
}
