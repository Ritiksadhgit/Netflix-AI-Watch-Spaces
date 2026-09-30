package com.netflix.watchspaces.domain.dto.response;

import com.netflix.watchspaces.domain.entity.VariationOption;

public class VariationOptionResponse {

    private Long id;
    private Long timelineEventId;
    private String label;
    private String assetRef;
    private Integer voteCount;

    public VariationOptionResponse() {}

    public VariationOptionResponse(Long id, Long timelineEventId, String label, String assetRef, Integer voteCount) {
        this.id = id;
        this.timelineEventId = timelineEventId;
        this.label = label;
        this.assetRef = assetRef;
        this.voteCount = voteCount != null ? voteCount : 0;
    }

    public static VariationOptionResponse fromEntity(VariationOption entity) {
        return new VariationOptionResponse(
                entity.getId(),
                entity.getTimelineEvent() != null ? entity.getTimelineEvent().getId() : null,
                entity.getLabel(),
                entity.getAssetRef(),
                entity.getVoteCount()
        );
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTimelineEventId() { return timelineEventId; }
    public void setTimelineEventId(Long timelineEventId) { this.timelineEventId = timelineEventId; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getAssetRef() { return assetRef; }
    public void setAssetRef(String assetRef) { this.assetRef = assetRef; }

    public Integer getVoteCount() { return voteCount; }
    public void setVoteCount(Integer voteCount) { this.voteCount = voteCount; }
}
