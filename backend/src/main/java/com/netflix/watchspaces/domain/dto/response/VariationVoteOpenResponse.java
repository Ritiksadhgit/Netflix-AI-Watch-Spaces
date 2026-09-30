package com.netflix.watchspaces.domain.dto.response;

import java.util.ArrayList;
import java.util.List;

public class VariationVoteOpenResponse {

    private Long variationId;
    private String prompt;
    private List<VariationOptionResponse> options = new ArrayList<>();
    private Integer durationSeconds;
    private Long expiresAt;

    public VariationVoteOpenResponse() {}

    public VariationVoteOpenResponse(Long variationId, String prompt, List<VariationOptionResponse> options, Integer durationSeconds, Long expiresAt) {
        this.variationId = variationId;
        this.prompt = prompt;
        this.options = options != null ? options : new ArrayList<>();
        this.durationSeconds = durationSeconds != null ? durationSeconds : 20;
        this.expiresAt = expiresAt;
    }

    public Long getVariationId() { return variationId; }
    public void setVariationId(Long variationId) { this.variationId = variationId; }

    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }

    public List<VariationOptionResponse> getOptions() { return options; }
    public void setOptions(List<VariationOptionResponse> options) { this.options = options; }

    public Integer getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }

    public Long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Long expiresAt) { this.expiresAt = expiresAt; }
}
