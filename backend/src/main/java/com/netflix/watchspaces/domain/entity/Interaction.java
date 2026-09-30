package com.netflix.watchspaces.domain.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "interactions")
public class Interaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "title_id", nullable = false)
    private Title title;

    @Column(name = "watched_seconds", nullable = false)
    private Integer watchedSeconds = 0;

    @Column(nullable = false)
    private boolean completed = false;

    private Integer rating;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Interaction() {}

    public Interaction(Long id, User user, Title title, Integer watchedSeconds, boolean completed, Integer rating, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.user = user;
        this.title = title;
        this.watchedSeconds = watchedSeconds != null ? watchedSeconds : 0;
        this.completed = completed;
        this.rating = rating;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private User user;
        private Title title;
        private Integer watchedSeconds = 0;
        private boolean completed = false;
        private Integer rating;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder user(User user) { this.user = user; return this; }
        public Builder title(Title title) { this.title = title; return this; }
        public Builder watchedSeconds(Integer watchedSeconds) { this.watchedSeconds = watchedSeconds; return this; }
        public Builder completed(boolean completed) { this.completed = completed; return this; }
        public Builder rating(Integer rating) { this.rating = rating; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public Interaction build() {
            return new Interaction(id, user, title, watchedSeconds, completed, rating, createdAt, updatedAt);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Title getTitle() { return title; }
    public void setTitle(Title title) { this.title = title; }

    public Integer getWatchedSeconds() { return watchedSeconds; }
    public void setWatchedSeconds(Integer watchedSeconds) { this.watchedSeconds = watchedSeconds; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
