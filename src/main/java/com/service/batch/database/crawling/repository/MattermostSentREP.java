package com.service.batch.database.crawling.repository;

import com.service.batch.database.crawling.entity.MattermostSentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Repository
public interface MattermostSentREP extends JpaRepository<MattermostSentEntity, Long> {
    @Modifying(clearAutomatically = true)
    @Transactional("crawlingTransactionManager")
    @Query("DELETE FROM MattermostSentEntity e WHERE e.category IN :categories")
    int deleteAllByCategoryIn(@NonNull @Param("categories") Collection<String> categories);
}
