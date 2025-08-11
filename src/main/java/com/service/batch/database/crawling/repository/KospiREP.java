package com.service.batch.database.crawling.repository;//package com.example.batch.common.database.rep.jpa.kospi;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//
//@Repository
//public interface KospiREP extends JpaRepository<KospiEntity, Long> {
//    Optional<KospiEntity> findTop1ByOrderByIdDesc();
//    @Query("select t from KospiEntity t where t.createDate BETWEEN :date AND NOW() AND function('date_format', t.createDate, '%T') between '09:00:00' and '09:01:00' order by t.createDate desc")
//    List<KospiEntity> findKospiEntityByCreateDateBetweenAndCreateDateBetweenFirst(@Param("date") LocalDateTime date);
//    @Query("select t from KospiEntity t where t.createDate BETWEEN :date AND NOW() AND function('date_format', t.createDate, '%T') between '15:00:00' and '15:01:00' order by t.createDate desc")
//    List<KospiEntity> findKospiEntityByCreateDateBetweenAndCreateDateBetweenLast(@Param("date") LocalDateTime date);
//}
