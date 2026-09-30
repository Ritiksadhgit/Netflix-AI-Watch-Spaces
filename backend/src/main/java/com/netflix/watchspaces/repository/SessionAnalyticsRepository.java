package com.netflix.watchspaces.repository;

import com.netflix.watchspaces.domain.entity.SessionAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SessionAnalyticsRepository extends JpaRepository<SessionAnalytics, Long> {
    Optional<SessionAnalytics> findByWatchSpaceId(String watchSpaceId);
}
