package com.netflix.watchspaces.websocket.event;

import java.time.Instant;

public class WebSocketEnvelope<T> {
    private String event;
    private String watchSpaceId;
    private T payload;
    private long ts = Instant.now().toEpochMilli();

    public WebSocketEnvelope() {}

    public WebSocketEnvelope(String event, String watchSpaceId, T payload) {
        this.event = event;
        this.watchSpaceId = watchSpaceId;
        this.payload = payload;
        this.ts = Instant.now().toEpochMilli();
    }

    public WebSocketEnvelope(String event, String watchSpaceId, T payload, long ts) {
        this.event = event;
        this.watchSpaceId = watchSpaceId;
        this.payload = payload;
        this.ts = ts;
    }

    public static <T> WebSocketEnvelope<T> of(String event, String watchSpaceId, T payload) {
        return new WebSocketEnvelope<>(event, watchSpaceId, payload);
    }

    public String getEvent() { return event; }
    public void setEvent(String event) { this.event = event; }

    public String getWatchSpaceId() { return watchSpaceId; }
    public void setWatchSpaceId(String watchSpaceId) { this.watchSpaceId = watchSpaceId; }

    public T getPayload() { return payload; }
    public void setPayload(T payload) { this.payload = payload; }

    public long getTs() { return ts; }
    public void setTs(long ts) { this.ts = ts; }
}
