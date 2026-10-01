package com.netflix.watchspaces.repository;

import com.netflix.watchspaces.domain.entity.WatchSpaceParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WatchSpaceParticipantRepository extends JpaRepository<WatchSpaceParticipant, Long> {

    @Query("SELECT p FROM WatchSpaceParticipant p JOIN FETCH p.user WHERE p.watchSpace.id = :watchSpaceId AND p.leftAt IS NULL")
    List<WatchSpaceParticipant> findActiveByWatchSpaceId(String watchSpaceId);

    @Query("SELECT p FROM WatchSpaceParticipant p JOIN FETCH p.user WHERE p.watchSpace.id = :watchSpaceId AND p.user.id = :userId AND p.leftAt IS NULL")
    Optional<WatchSpaceParticipant> findActiveByWatchSpaceIdAndUserId(String watchSpaceId, Long userId);
}
