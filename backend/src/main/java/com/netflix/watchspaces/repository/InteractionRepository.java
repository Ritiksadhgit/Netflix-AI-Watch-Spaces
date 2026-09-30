package com.netflix.watchspaces.repository;

import com.netflix.watchspaces.domain.entity.Interaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InteractionRepository extends JpaRepository<Interaction, Long> {
    List<Interaction> findByUserId(Long userId);
    List<Interaction> findByTitleId(Long titleId);
    Optional<Interaction> findByUserIdAndTitleId(Long userId, Long titleId);
}
