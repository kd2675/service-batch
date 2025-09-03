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
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.profiles.active=test")
@Transactional
@Rollback
@EntityScan(basePackages = "com.service.batch.database.crawling.entity")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY) // H2 인메모리 DB 사용
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

        repository.save(MattermostSentEntity.builder().sentId("a").category("news").build());
        repository.save(MattermostSentEntity.builder().sentId("b").category("news").build());
        repository.save(MattermostSentEntity.builder().sentId("c").category("news").build());

        // when
        JpaPagingItemReader<MattermostSentEntity> reader = new MattermostReader().findByCategoryIsNews(entityManagerFactory);
        reader.afterPropertiesSet();
        reader.open(new ExecutionContext());
        MattermostSentEntity r1 = reader.read();
        MattermostSentEntity r2 = reader.read();
        MattermostSentEntity r3 = reader.read();

        // then
        // r1, r2는 24시간 이전(createDate가 now-24h 보다 전)
        assertThat(r1).isNotNull();
        assertThat(r1.getCreateDate()).isBefore(now.minusHours(24));
        assertThat(r2).isNotNull();
        assertThat(r2.getCreateDate()).isBefore(now.minusHours(24));

        // r3는 더 이상 없음(null)
        assertThat(r3).isNull();
    }
}