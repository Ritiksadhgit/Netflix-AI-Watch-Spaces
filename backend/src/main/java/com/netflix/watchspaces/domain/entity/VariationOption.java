package com.netflix.watchspaces.domain.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "variation_options")
public class VariationOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timeline_event_id", nullable = false)
    private TimelineEvent timelineEvent;

    @Column(nullable = false)
    private String label;

    @Column(name = "asset_ref", nullable = false, length = 1024)
    private String assetRef;

    @Column(name = "vote_count", nullable = false)
    private Integer voteCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public VariationOption() {}

    public VariationOption(Long id, TimelineEvent timelineEvent, String label, String assetRef, Integer voteCount, LocalDateTime createdAt) {
        this.id = id;
        this.timelineEvent = timelineEvent;
        this.label = label;
        this.assetRef = assetRef;
        this.voteCount = voteCount != null ? voteCount : 0;
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private TimelineEvent timelineEvent;
        private String label;
        private String assetRef;
        private Integer voteCount = 0;
        private LocalDateTime createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder timelineEvent(TimelineEvent timelineEvent) { this.timelineEvent = timelineEvent; return this; }
        public Builder label(String label) { this.label = label; return this; }
        public Builder assetRef(String assetRef) { this.assetRef = assetRef; return this; }
        public Builder voteCount(Integer voteCount) { this.voteCount = voteCount; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public VariationOption build() {
            return new VariationOption(id, timelineEvent, label, assetRef, voteCount, createdAt);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TimelineEvent getTimelineEvent() { return timelineEvent; }
    public void setTimelineEvent(TimelineEvent timelineEvent) { this.timelineEvent = timelineEvent; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getAssetRef() { return assetRef; }
    public void setAssetRef(String assetRef) { this.assetRef = assetRef; }

    public Integer getVoteCount() { return voteCount; }
    public void setVoteCount(Integer voteCount) { this.voteCount = voteCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
