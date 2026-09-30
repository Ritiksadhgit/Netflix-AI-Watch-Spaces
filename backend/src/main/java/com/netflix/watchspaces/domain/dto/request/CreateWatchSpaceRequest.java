package com.netflix.watchspaces.domain.dto.request;

import com.netflix.watchspaces.domain.enums.AiVerbosity;
import com.netflix.watchspaces.domain.enums.PrivacyType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateWatchSpaceRequest {

    @NotNull(message = "Title ID is required")
    private Long titleId;

    @NotBlank(message = "Watch Space name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;

    private PrivacyType privacy = PrivacyType.PUBLIC;

    @Min(value = 2, message = "Must allow at least 2 participants")
    @Max(value = 200, message = "Maximum allowed participants is 200")
    private Integer maxParticipants = 50;

    private AiVerbosity aiVerbosity = AiVerbosity.NORMAL;

    private boolean votingEnabled = true;

    public CreateWatchSpaceRequest() {}

    public CreateWatchSpaceRequest(Long titleId, String name, PrivacyType privacy, Integer maxParticipants, AiVerbosity aiVerbosity, boolean votingEnabled) {
        this.titleId = titleId;
        this.name = name;
        this.privacy = privacy != null ? privacy : PrivacyType.PUBLIC;
        this.maxParticipants = maxParticipants != null ? maxParticipants : 50;
        this.aiVerbosity = aiVerbosity != null ? aiVerbosity : AiVerbosity.NORMAL;
        this.votingEnabled = votingEnabled;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long titleId;
        private String name;
        private PrivacyType privacy = PrivacyType.PUBLIC;
        private Integer maxParticipants = 50;
        private AiVerbosity aiVerbosity = AiVerbosity.NORMAL;
        private boolean votingEnabled = true;

        public Builder titleId(Long titleId) { this.titleId = titleId; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder privacy(PrivacyType privacy) { this.privacy = privacy; return this; }
        public Builder maxParticipants(Integer maxParticipants) { this.maxParticipants = maxParticipants; return this; }
        public Builder aiVerbosity(AiVerbosity aiVerbosity) { this.aiVerbosity = aiVerbosity; return this; }
        public Builder votingEnabled(boolean votingEnabled) { this.votingEnabled = votingEnabled; return this; }

        public CreateWatchSpaceRequest build() {
            return new CreateWatchSpaceRequest(titleId, name, privacy, maxParticipants, aiVerbosity, votingEnabled);
        }
    }

    public Long getTitleId() { return titleId; }
    public void setTitleId(Long titleId) { this.titleId = titleId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public PrivacyType getPrivacy() { return privacy; }
    public void setPrivacy(PrivacyType privacy) { this.privacy = privacy; }

    public Integer getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(Integer maxParticipants) { this.maxParticipants = maxParticipants; }

    public AiVerbosity getAiVerbosity() { return aiVerbosity; }
    public void setAiVerbosity(AiVerbosity aiVerbosity) { this.aiVerbosity = aiVerbosity; }

    public boolean isVotingEnabled() { return votingEnabled; }
    public void setVotingEnabled(boolean votingEnabled) { this.votingEnabled = votingEnabled; }
}
