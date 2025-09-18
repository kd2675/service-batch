package com.service.batch.cron.reader;

import com.service.batch.database.crawling.entity.MattermostSentEntity;
import com.service.batch.database.crawling.repository.MattermostSentREP;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.profiles.active=test")
//@Import({JpaAuditingConfig.class})
@Transactional
@Rollback
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY) // H2 인메모리 DB 사용
@EnableJpaRepositories(basePackages = "com.service.batch.database.crawling.repository")
@EntityScan(basePackages = "com.service.batch.database.crawling.entity")
class MattermostReaderTest {
    @Autowired
    MattermostSentREP repository;
    @Autowired
    EntityManagerFactory entityManagerFactory;

    @BeforeEach
    void clear() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("가져온값이_모두_24시간_이전인지")
    void findByCategoryIsNews() throws Exception{
        // given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime 기준1 = now.minusHours(18); // 18시간 전 (최근)
        LocalDateTime 기준2 = now.minusHours(26); // 26시간 전 (old)
        LocalDateTime 기준3 = now.minusHours(30); // 30시간 전 (older)

        MattermostSentEntity e1 = MattermostSentEntity.builder().sentId("a").category("news").build();
        MattermostSentEntity e2 = MattermostSentEntity.builder().sentId("b").category("news").build();
        MattermostSentEntity e3 = MattermostSentEntity.builder().sentId("c").category("news").build();

        setDates(e1, 기준1, 기준1);
        setDates(e2, 기준2, 기준2);
        setDates(e3, 기준3, 기준3);

        repository.save(e1);
        repository.save(e2);
        repository.save(e3);
        repository.flush(); // flush는 커밋이 아님

        TestTransaction.flagForCommit();// 현재 테스트 트랜잭션을 커밋하여 다른 커넥션에서도 보이게 함
        TestTransaction.end();     // 실제 커밋 수행
        TestTransaction.start();   // 필요 시 새 트랜잭션 시작

        // when
        JpaPagingItemReader<MattermostSentEntity> reader = new MattermostReader().findByCategoryIsNews(entityManagerFactory);
        // 테스트 기준(now)과 동일한 파라미터 사용
        HashMap<String, Object> params = new HashMap<>();
        params.put("date", now.minusHours(24));
        reader.setParameterValues(params);

        reader.setTransacted(false);
        reader.afterPropertiesSet();
        reader.open(new ExecutionContext());
        MattermostSentEntity r1 = reader.read();
        MattermostSentEntity r2 = reader.read();
        MattermostSentEntity r3 = reader.read();
        reader.close();

        // then
        assertThat(r1).isNotNull();
        assertThat(r1.getCreateDate()).isBefore(now.minusHours(24));
        assertThat(r2).isNotNull();
        assertThat(r2.getCreateDate()).isBefore(now.minusHours(24));
        assertThat(r3).isNull();
    }

    // 상위 클래스의 private 필드(createDate, updateDate) 세팅 유틸
    private static void setDates(Object target, LocalDateTime create, LocalDateTime update) throws Exception {
        Class<?> clazz = target.getClass().getSuperclass(); // CommonDateEntity
        Field createDate = clazz.getDeclaredField("createDate");
        Field updateDate = clazz.getDeclaredField("updateDate");
        createDate.setAccessible(true);
        updateDate.setAccessible(true);
        createDate.set(target, create);
        updateDate.set(target, update);
    }
}