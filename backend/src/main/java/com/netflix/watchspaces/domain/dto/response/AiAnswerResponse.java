package com.netflix.watchspaces.domain.dto.response;

import java.util.ArrayList;
import java.util.List;

public class AiAnswerResponse {

    private String answer;
    private String currentScene;
    private Double timestamp;
    private List<AiSourceCitation> sources = new ArrayList<>();

    public AiAnswerResponse() {}

    public AiAnswerResponse(String answer, String currentScene, Double timestamp, List<AiSourceCitation> sources) {
        this.answer = answer;
        this.currentScene = currentScene;
        this.timestamp = timestamp;
        this.sources = sources != null ? sources : new ArrayList<>();
    }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public String getCurrentScene() { return currentScene; }
    public void setCurrentScene(String currentScene) { this.currentScene = currentScene; }

    public Double getTimestamp() { return timestamp; }
    public void setTimestamp(Double timestamp) { this.timestamp = timestamp; }

    public List<AiSourceCitation> getSources() { return sources; }
    public void setSources(List<AiSourceCitation> sources) { this.sources = sources; }
}
