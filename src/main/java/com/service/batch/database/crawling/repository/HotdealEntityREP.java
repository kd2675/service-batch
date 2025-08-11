package com.service.batch.database.crawling.repository;

import com.service.batch.database.crawling.entity.HotdealEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HotdealEntityREP extends JpaRepository<HotdealEntity, Long> {
    List<HotdealEntity> findTop1ByOrderByProductIdDesc();

    Page<HotdealEntity> findAll(Specification<HotdealEntity> spec, Pageable pageable);

    List<HotdealEntity> findTop5BySendYnOrderByIdDesc(String sendYn);

    List<HotdealEntity> findAllBySendYnOrderByIdDesc(String sendYn);
}