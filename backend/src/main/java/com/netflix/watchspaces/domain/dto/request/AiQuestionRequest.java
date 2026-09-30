package com.netflix.watchspaces.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AiQuestionRequest {

    @NotBlank(message = "Question cannot be blank")
    @Size(min = 3, max = 250, message = "Question must be between 3 and 250 characters")
    private String question;

    private Double currentTimestamp = 0.0;

    public AiQuestionRequest() {}

    public AiQuestionRequest(String question, Double currentTimestamp) {
        this.question = question;
        this.currentTimestamp = currentTimestamp != null ? currentTimestamp : 0.0;
    }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public Double getCurrentTimestamp() { return currentTimestamp; }
    public void setCurrentTimestamp(Double currentTimestamp) { this.currentTimestamp = currentTimestamp; }
}
