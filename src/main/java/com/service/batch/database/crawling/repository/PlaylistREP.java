package com.service.batch.database.crawling.repository;

import com.service.batch.database.crawling.entity.PlaylistEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.Optional;

public interface PlaylistREP extends JpaRepository<PlaylistEntity, Long> {
    Optional<PlaylistEntity> findByNo(@NonNull Long no);

}