package com.service.batch.database.crawling.repository;

import com.service.batch.database.crawling.entity.MattermostSentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MattermostSentREP extends JpaRepository<MattermostSentEntity, Long> {
    void deleteAllByCategory(@NonNull String category);

    List<MattermostSentEntity> findAllByCategory(String category);
}
