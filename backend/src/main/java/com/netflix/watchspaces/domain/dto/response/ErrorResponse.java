package com.netflix.watchspaces.domain.dto.response;

import java.time.Instant;
import java.util.Map;

public class ErrorResponse {
    private String type;
    private String title;
    private int status;
    private String detail;
    private String instance;
    private String code;
    private String timestamp = Instant.now().toString();
    private String correlationId;
    private Map<String, String> validationErrors;

    public ErrorResponse() {}

    public ErrorResponse(String type, String title, int status, String detail, String instance, String code, String timestamp, String correlationId, Map<String, String> validationErrors) {
        this.type = type;
        this.title = title;
        this.status = status;
        this.detail = detail;
        this.instance = instance;
        this.code = code;
        this.timestamp = timestamp != null ? timestamp : Instant.now().toString();
        this.correlationId = correlationId;
        this.validationErrors = validationErrors;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String type;
        private String title;
        private int status;
        private String detail;
        private String instance;
        private String code;
        private String timestamp = Instant.now().toString();
        private String correlationId;
        private Map<String, String> validationErrors;

        public Builder type(String type) { this.type = type; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder status(int status) { this.status = status; return this; }
        public Builder detail(String detail) { this.detail = detail; return this; }
        public Builder instance(String instance) { this.instance = instance; return this; }
        public Builder code(String code) { this.code = code; return this; }
        public Builder timestamp(String timestamp) { this.timestamp = timestamp; return this; }
        public Builder correlationId(String correlationId) { this.correlationId = correlationId; return this; }
        public Builder validationErrors(Map<String, String> validationErrors) { this.validationErrors = validationErrors; return this; }

        public ErrorResponse build() {
            return new ErrorResponse(type, title, status, detail, instance, code, timestamp, correlationId, validationErrors);
        }
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }

    public String getInstance() { return instance; }
    public void setInstance(String instance) { this.instance = instance; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public Map<String, String> getValidationErrors() { return validationErrors; }
    public void setValidationErrors(Map<String, String> validationErrors) { this.validationErrors = validationErrors; }
}
