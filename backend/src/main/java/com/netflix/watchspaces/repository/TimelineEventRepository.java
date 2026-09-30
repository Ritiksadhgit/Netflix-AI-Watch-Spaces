package com.netflix.watchspaces.repository;

import com.netflix.watchspaces.domain.entity.TimelineEvent;
import com.netflix.watchspaces.domain.enums.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimelineEventRepository extends JpaRepository<TimelineEvent, Long> {

    List<TimelineEvent> findByTitleIdOrderByTsSecondsAsc(Long titleId);

    @Query("SELECT e FROM TimelineEvent e WHERE e.title.id = :titleId AND e.tsSeconds BETWEEN :fromSec AND :toSec ORDER BY e.tsSeconds ASC")
    List<TimelineEvent> findByTitleIdAndTsRange(Long titleId, Integer fromSec, Integer toSec);

    List<TimelineEvent> findByTitleIdAndEventType(Long titleId, EventType eventType);
}
