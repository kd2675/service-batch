package com.service.batch.database.crawling.repository;

import com.service.batch.database.crawling.entity.HotdealAlimEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.Nullable;

import java.util.List;

public interface HotdealAlimEntityREP extends JpaRepository<HotdealAlimEntity, Long> {
    List<HotdealAlimEntity> findBySendYn(@Nullable String sendYn);

    List<HotdealAlimEntity> findByTargetAndSendYn(@Nullable String target, @Nullable String sendYn);

    HotdealAlimEntity findByKeywordAndTargetAndSendYn(@Nullable String keyword, @Nullable String target, @Nullable String sendYn);

}