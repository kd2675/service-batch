package com.service.batch.database.crawling.repository;

import com.service.batch.database.crawling.entity.OldNewsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OldNewsREP extends JpaRepository<OldNewsEntity, Long>, JpaSpecificationExecutor<OldNewsEntity> {
    List<OldNewsEntity> findByTitleLikeOrContentLikeOrderByIdDesc(@Nullable String title, @Nullable String content, Pageable pageable);

    @Query(value = "SELECT e.* FROM OLD_NEWS e WHERE regexp_like(e.title, :text) ORDER BY e.id DESC", nativeQuery = true)
    List<OldNewsEntity> search(@Param("text") String text, Pageable pageable);

    Page<OldNewsEntity> findAll(Specification<OldNewsEntity> spec, Pageable pageable);


}
