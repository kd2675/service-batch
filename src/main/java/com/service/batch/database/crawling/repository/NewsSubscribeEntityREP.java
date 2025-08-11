package com.service.batch.database.crawling.repository;

import com.service.batch.database.crawling.entity.NewsSubscribeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsSubscribeEntityREP extends JpaRepository<NewsSubscribeEntity, Long> {
}