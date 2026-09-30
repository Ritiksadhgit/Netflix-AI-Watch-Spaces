package com.netflix.watchspaces.domain.dto.response;

import com.netflix.watchspaces.domain.entity.ChatMessage;

import java.time.LocalDateTime;

public class ChatMessageResponse {

    private Long id;
    private String watchSpaceId;
    private Long userId;
    private String senderName;
    private String msgType;
    private String body;
    private Double tsSeconds;
    private LocalDateTime createdAt;

    public ChatMessageResponse() {}

    public ChatMessageResponse(Long id, String watchSpaceId, Long userId, String senderName, String msgType, String body, Double tsSeconds, LocalDateTime createdAt) {
        this.id = id;
        this.watchSpaceId = watchSpaceId;
        this.userId = userId;
        this.senderName = senderName;
        this.msgType = msgType;
        this.body = body;
        this.tsSeconds = tsSeconds;
        this.createdAt = createdAt;
    }

    public static ChatMessageResponse fromEntity(ChatMessage msg) {
        return new ChatMessageResponse(
                msg.getId(),
                msg.getWatchSpace() != null ? msg.getWatchSpace().getId() : null,
                msg.getUser() != null ? msg.getUser().getId() : null,
                msg.getSenderName(),
                msg.getMsgType() != null ? msg.getMsgType().name() : "USER",
                msg.getBody(),
                msg.getTsSeconds(),
                msg.getCreatedAt()
        );
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getWatchSpaceId() { return watchSpaceId; }
    public void setWatchSpaceId(String watchSpaceId) { this.watchSpaceId = watchSpaceId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getMsgType() { return msgType; }
    public void setMsgType(String msgType) { this.msgType = msgType; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public Double getTsSeconds() { return tsSeconds; }
    public void setTsSeconds(Double tsSeconds) { this.tsSeconds = tsSeconds; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
