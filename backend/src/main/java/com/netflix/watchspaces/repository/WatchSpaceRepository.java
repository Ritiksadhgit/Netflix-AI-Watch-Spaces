package com.netflix.watchspaces.repository;

import com.netflix.watchspaces.domain.entity.WatchSpace;
import com.netflix.watchspaces.domain.enums.WatchSpaceStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WatchSpaceRepository extends JpaRepository<WatchSpace, String> {

    @EntityGraph(attributePaths = {"title", "hostUser"})
    Optional<WatchSpace> findById(String id);

    @EntityGraph(attributePaths = {"title", "hostUser"})
    Optional<WatchSpace> findByInviteCode(String inviteCode);

    @EntityGraph(attributePaths = {"title", "hostUser"})
    List<WatchSpace> findByStatus(WatchSpaceStatus status);

    @EntityGraph(attributePaths = {"title", "hostUser"})
    List<WatchSpace> findByStatusOrderByCreatedAtDesc(WatchSpaceStatus status);
}
