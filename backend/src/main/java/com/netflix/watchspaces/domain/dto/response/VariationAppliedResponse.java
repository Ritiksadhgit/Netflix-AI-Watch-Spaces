package com.netflix.watchspaces.domain.dto.response;

public class VariationAppliedResponse {

    private Long variationId;
    private Long winningOptionId;
    private String winningLabel;
    private Integer totalVotes;
    private String assetRef;
    private String action;

    public VariationAppliedResponse() {}

    public VariationAppliedResponse(Long variationId, Long winningOptionId, String winningLabel, Integer totalVotes, String assetRef, String action) {
        this.variationId = variationId;
        this.winningOptionId = winningOptionId;
        this.winningLabel = winningLabel;
        this.totalVotes = totalVotes;
        this.assetRef = assetRef;
        this.action = action != null ? action : "NARRATIVE_BRANCH_APPLIED";
    }

    public Long getVariationId() { return variationId; }
    public void setVariationId(Long variationId) { this.variationId = variationId; }

    public Long getWinningOptionId() { return winningOptionId; }
    public void setWinningOptionId(Long winningOptionId) { this.winningOptionId = winningOptionId; }

    public String getWinningLabel() { return winningLabel; }
    public void setWinningLabel(String winningLabel) { this.winningLabel = winningLabel; }

    public Integer getTotalVotes() { return totalVotes; }
    public void setTotalVotes(Integer totalVotes) { this.totalVotes = totalVotes; }

    public String getAssetRef() { return assetRef; }
    public void setAssetRef(String assetRef) { this.assetRef = assetRef; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
}
