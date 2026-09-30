package com.netflix.watchspaces.repository;

import com.netflix.watchspaces.domain.entity.WatchSpace;
import com.netflix.watchspaces.domain.enums.WatchSpaceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WatchSpaceRepository extends JpaRepository<WatchSpace, String> {
    Optional<WatchSpace> findByInviteCode(String inviteCode);
    List<WatchSpace> findByStatus(WatchSpaceStatus status);
}
