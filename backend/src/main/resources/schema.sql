-- Schema for Netflix AI Watch Spaces (MySQL 8/9 Compliant)

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(191) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    role VARCHAR(32) NOT NULL DEFAULT 'VIEWER',
    subtitle_locale VARCHAR(10) NOT NULL DEFAULT 'en-US',
    avatar_url VARCHAR(512),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_role (role)
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_refresh_token_user (user_id, revoked)
);

CREATE TABLE IF NOT EXISTS titles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    synopsis TEXT NOT NULL,
    duration_seconds INT NOT NULL,
    video_asset_url VARCHAR(1024) NOT NULL,
    poster_url VARCHAR(1024),
    backdrop_url VARCHAR(1024),
    genres VARCHAR(255) NOT NULL,
    rating_code VARCHAR(16) DEFAULT 'PG-13',
    release_year INT DEFAULT 2024,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_titles_genres (genres)
);

CREATE TABLE IF NOT EXISTS timeline_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title_id BIGINT NOT NULL,
    ts_seconds INT NOT NULL,
    event_type VARCHAR(32) NOT NULL,
    title VARCHAR(255) NOT NULL,
    payload JSON NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (title_id) REFERENCES titles(id) ON DELETE CASCADE,
    INDEX idx_timeline_lookup (title_id, ts_seconds, event_type)
);

CREATE TABLE IF NOT EXISTS variation_options (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    timeline_event_id BIGINT NOT NULL,
    label VARCHAR(255) NOT NULL,
    asset_ref VARCHAR(1024) NOT NULL,
    vote_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (timeline_event_id) REFERENCES timeline_events(id) ON DELETE CASCADE,
    INDEX idx_variation_event (timeline_event_id)
);

CREATE TABLE IF NOT EXISTS watch_spaces (
    id VARCHAR(36) PRIMARY KEY,
    title_id BIGINT NOT NULL,
    host_user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'LIVE',
    privacy VARCHAR(32) NOT NULL DEFAULT 'PUBLIC',
    invite_code VARCHAR(64) NOT NULL UNIQUE,
    max_participants INT NOT NULL DEFAULT 50,
    is_locked BOOLEAN NOT NULL DEFAULT FALSE,
    ai_verbosity VARCHAR(32) NOT NULL DEFAULT 'NORMAL',
    voting_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    playback_state VARCHAR(32) NOT NULL DEFAULT 'PAUSED',
    playback_position_seconds DOUBLE NOT NULL DEFAULT 0.0,
    playback_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ended_at TIMESTAMP NULL,
    FOREIGN KEY (title_id) REFERENCES titles(id),
    FOREIGN KEY (host_user_id) REFERENCES users(id),
    INDEX idx_watchspace_status (status),
    INDEX idx_watchspace_invite (invite_code)
);

CREATE TABLE IF NOT EXISTS watch_space_participants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    watch_space_id VARCHAR(36) NOT NULL,
    user_id BIGINT NOT NULL,
    is_muted BOOLEAN NOT NULL DEFAULT FALSE,
    role_in_room VARCHAR(32) NOT NULL DEFAULT 'PARTICIPANT',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    left_at TIMESTAMP NULL,
    last_ping_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (watch_space_id) REFERENCES watch_spaces(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_participant_active (watch_space_id, user_id, left_at)
);

CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    watch_space_id VARCHAR(36) NOT NULL,
    user_id BIGINT NULL,
    sender_name VARCHAR(100) NOT NULL,
    msg_type VARCHAR(32) NOT NULL DEFAULT 'USER',
    body TEXT NOT NULL,
    ts_seconds DOUBLE NOT NULL DEFAULT 0.0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (watch_space_id) REFERENCES watch_spaces(id) ON DELETE CASCADE,
    INDEX idx_chat_space_time (watch_space_id, created_at)
);

CREATE TABLE IF NOT EXISTS interactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title_id BIGINT NOT NULL,
    watched_seconds INT NOT NULL DEFAULT 0,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    rating INT DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (title_id) REFERENCES titles(id) ON DELETE CASCADE,
    INDEX idx_interactions_user (user_id, completed),
    INDEX idx_interactions_title (title_id)
);

CREATE TABLE IF NOT EXISTS session_analytics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    watch_space_id VARCHAR(36) NOT NULL UNIQUE,
    title_id BIGINT NOT NULL,
    host_user_id BIGINT NOT NULL,
    session_duration_seconds INT NOT NULL DEFAULT 0,
    peak_participants INT NOT NULL DEFAULT 0,
    ai_questions_count INT NOT NULL DEFAULT 0,
    trivia_shown_count INT NOT NULL DEFAULT 0,
    chat_messages_count INT NOT NULL DEFAULT 0,
    votes_cast_count INT NOT NULL DEFAULT 0,
    event_log JSON NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (watch_space_id) REFERENCES watch_spaces(id) ON DELETE CASCADE,
    INDEX idx_analytics_title (title_id)
);
