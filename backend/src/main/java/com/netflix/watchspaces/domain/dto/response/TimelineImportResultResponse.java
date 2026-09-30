package com.netflix.watchspaces.domain.dto.response;

public class TimelineImportResultResponse {

    private Long titleId;
    private int importedCount;
    private boolean replacedExisting;
    private String status;
    private String message;

    public TimelineImportResultResponse() {}

    public TimelineImportResultResponse(Long titleId, int importedCount, boolean replacedExisting, String status, String message) {
        this.titleId = titleId;
        this.importedCount = importedCount;
        this.replacedExisting = replacedExisting;
        this.status = status;
        this.message = message;
    }

    public Long getTitleId() { return titleId; }
    public void setTitleId(Long titleId) { this.titleId = titleId; }

    public int getImportedCount() { return importedCount; }
    public void setImportedCount(int importedCount) { this.importedCount = importedCount; }

    public boolean isReplacedExisting() { return replacedExisting; }
    public void setReplacedExisting(boolean replacedExisting) { this.replacedExisting = replacedExisting; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
