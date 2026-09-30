package com.netflix.watchspaces.repository;

import com.netflix.watchspaces.domain.entity.VariationOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VariationOptionRepository extends JpaRepository<VariationOption, Long> {
    List<VariationOption> findByTimelineEventId(Long timelineEventId);
}
