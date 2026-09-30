package com.netflix.watchspaces.repository;

import com.netflix.watchspaces.domain.entity.Title;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TitleRepository extends JpaRepository<Title, Long> {
    List<Title> findByGenresContainingIgnoreCase(String genre);
}
