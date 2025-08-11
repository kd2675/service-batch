package com.service.batch.database.crawling.repository;

import com.service.batch.database.crawling.entity.WatchEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface WatchREP extends JpaRepository<WatchEntity, Long> {
    @Query(value = "select e from WatchEntity e order by rand() limit 1")
    Optional<WatchEntity> findWatchRand();

    List<WatchEntity> findByWatchYnOrderByIdDesc(@NonNull String watchYn, Pageable pageable);


}