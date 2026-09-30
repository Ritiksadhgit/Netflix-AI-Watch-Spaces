package com.netflix.watchspaces.domain.entity;

import com.netflix.watchspaces.domain.enums.EventType;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "timeline_events")
public class TimelineEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "title_id", nullable = false)
    private Title title;

    @Column(name = "ts_seconds", nullable = false)
    private Integer tsSeconds;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 32)
    private EventType eventType;

    @Column(name = "title", nullable = false)
    private String eventTitle;

    @Column(name = "payload", nullable = false, columnDefinition = "JSON")
    private String payloadJson;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public TimelineEvent() {}

    public TimelineEvent(Long id, Title title, Integer tsSeconds, EventType eventType, String eventTitle, String payloadJson, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.tsSeconds = tsSeconds;
        this.eventType = eventType;
        this.eventTitle = eventTitle;
        this.payloadJson = payloadJson;
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Title title;
        private Integer tsSeconds;
        private EventType eventType;
        private String eventTitle;
        private String payloadJson;
        private LocalDateTime createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder title(Title title) { this.title = title; return this; }
        public Builder tsSeconds(Integer tsSeconds) { this.tsSeconds = tsSeconds; return this; }
        public Builder eventType(EventType eventType) { this.eventType = eventType; return this; }
        public Builder eventTitle(String eventTitle) { this.eventTitle = eventTitle; return this; }
        public Builder payloadJson(String payloadJson) { this.payloadJson = payloadJson; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public TimelineEvent build() {
            return new TimelineEvent(id, title, tsSeconds, eventType, eventTitle, payloadJson, createdAt);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Title getTitle() { return title; }
    public void setTitle(Title title) { this.title = title; }

    public Integer getTsSeconds() { return tsSeconds; }
    public void setTsSeconds(Integer tsSeconds) { this.tsSeconds = tsSeconds; }

    public EventType getEventType() { return eventType; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }

    public String getEventTitle() { return eventTitle; }
    public void setEventTitle(String eventTitle) { this.eventTitle = eventTitle; }

    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
