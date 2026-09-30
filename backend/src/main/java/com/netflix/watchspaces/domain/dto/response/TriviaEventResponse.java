package com.netflix.watchspaces.domain.dto.response;

import java.util.ArrayList;
import java.util.List;

public class TriviaEventResponse {

    private Long triviaId;
    private Long titleId;
    private Integer tsSeconds;
    private String question;
    private List<String> options = new ArrayList<>();
    private Integer answerIndex;
    private Integer durationSeconds;
    private String triviaNote;

    public TriviaEventResponse() {}

    public TriviaEventResponse(Long triviaId, Long titleId, Integer tsSeconds, String question, List<String> options, Integer answerIndex, Integer durationSeconds, String triviaNote) {
        this.triviaId = triviaId;
        this.titleId = titleId;
        this.tsSeconds = tsSeconds;
        this.question = question;
        this.options = options != null ? options : new ArrayList<>();
        this.answerIndex = answerIndex;
        this.durationSeconds = durationSeconds != null ? durationSeconds : 15;
        this.triviaNote = triviaNote;
    }

    public Long getTriviaId() { return triviaId; }
    public void setTriviaId(Long triviaId) { this.triviaId = triviaId; }

    public Long getTitleId() { return titleId; }
    public void setTitleId(Long titleId) { this.titleId = titleId; }

    public Integer getTsSeconds() { return tsSeconds; }
    public void setTsSeconds(Integer tsSeconds) { this.tsSeconds = tsSeconds; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public Integer getAnswerIndex() { return answerIndex; }
    public void setAnswerIndex(Integer answerIndex) { this.answerIndex = answerIndex; }

    public Integer getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }

    public String getTriviaNote() { return triviaNote; }
    public void setTriviaNote(String triviaNote) { this.triviaNote = triviaNote; }
}
