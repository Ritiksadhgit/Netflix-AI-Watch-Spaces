package com.netflix.watchspaces.domain.entity;

import com.netflix.watchspaces.domain.enums.MessageType;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "watch_space_id", nullable = false)
    private WatchSpace watchSpace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "sender_name", nullable = false, length = 100)
    private String senderName;

    @Enumerated(EnumType.STRING)
    @Column(name = "msg_type", nullable = false, length = 32)
    private MessageType msgType = MessageType.USER;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "ts_seconds", nullable = false)
    private Double tsSeconds = 0.0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public ChatMessage() {}

    public ChatMessage(Long id, WatchSpace watchSpace, User user, String senderName, MessageType msgType, String body, Double tsSeconds, LocalDateTime createdAt) {
        this.id = id;
        this.watchSpace = watchSpace;
        this.user = user;
        this.senderName = senderName;
        this.msgType = msgType != null ? msgType : MessageType.USER;
        this.body = body;
        this.tsSeconds = tsSeconds != null ? tsSeconds : 0.0;
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private WatchSpace watchSpace;
        private User user;
        private String senderName;
        private MessageType msgType = MessageType.USER;
        private String body;
        private Double tsSeconds = 0.0;
        private LocalDateTime createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder watchSpace(WatchSpace watchSpace) { this.watchSpace = watchSpace; return this; }
        public Builder user(User user) { this.user = user; return this; }
        public Builder senderName(String senderName) { this.senderName = senderName; return this; }
        public Builder msgType(MessageType msgType) { this.msgType = msgType; return this; }
        public Builder body(String body) { this.body = body; return this; }
        public Builder tsSeconds(Double tsSeconds) { this.tsSeconds = tsSeconds; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public ChatMessage build() {
            return new ChatMessage(id, watchSpace, user, senderName, msgType, body, tsSeconds, createdAt);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public WatchSpace getWatchSpace() { return watchSpace; }
    public void setWatchSpace(WatchSpace watchSpace) { this.watchSpace = watchSpace; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public MessageType getMsgType() { return msgType; }
    public void setMsgType(MessageType msgType) { this.msgType = msgType; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public Double getTsSeconds() { return tsSeconds; }
    public void setTsSeconds(Double tsSeconds) { this.tsSeconds = tsSeconds; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
