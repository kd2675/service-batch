package com.service.batch.database.crawling.repository;

import com.service.batch.database.crawling.entity.CoinEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface CoinREP extends JpaRepository<CoinEntity, Long> {
    public List<CoinEntity> findByCreateDateBefore(LocalDateTime dateTime);
    List<CoinEntity> findTop10ByOrderByIdDesc();
    List<CoinEntity> findTop1ByOrderByIdDesc();
    CoinEntity findTopByOrderByIdDesc();
    CoinEntity findTopByCoinSymbolOrderByIdDesc(String coinSymbol);

    @Query("SELECT c FROM CoinEntity c WHERE c.id IN (SELECT MAX(c2.id) FROM CoinEntity c2 WHERE c2.coinSymbol IN :coinSymbols GROUP BY c2.coinSymbol)")
    List<CoinEntity> findLatestByCoinSymbolIn(@Param("coinSymbols") Collection<String> coinSymbols);
}
