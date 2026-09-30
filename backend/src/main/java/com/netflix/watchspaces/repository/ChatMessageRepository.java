package com.netflix.watchspaces.repository;

import com.netflix.watchspaces.domain.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("SELECT m FROM ChatMessage m WHERE m.watchSpace.id = :watchSpaceId ORDER BY m.createdAt DESC")
    List<ChatMessage> findRecentByWatchSpaceId(String watchSpaceId, Pageable pageable);

    @Query("SELECT m FROM ChatMessage m WHERE m.watchSpace.id = :watchSpaceId AND m.id < :beforeId ORDER BY m.createdAt DESC")
    List<ChatMessage> findByWatchSpaceIdBefore(String watchSpaceId, Long beforeId, Pageable pageable);

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.watchSpace.id = :watchSpaceId")
    long countByWatchSpaceId(String watchSpaceId);
}
