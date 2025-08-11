package com.service.batch.database.crawling.repository;

import com.service.batch.database.crawling.entity.CoinEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CoinREP extends JpaRepository<CoinEntity, Long> {
    public List<CoinEntity> findByCreateDateBefore(LocalDateTime dateTime);
    List<CoinEntity> findTop10ByOrderByIdDesc();
    List<CoinEntity> findTop1ByOrderByIdDesc();
    CoinEntity findTopByOrderByIdDesc();
}
