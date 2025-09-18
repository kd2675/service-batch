package com.service.batch.cron.reader;

import com.service.batch.cron.common.DelJpaPagingItemReader;
import com.service.batch.database.crawling.entity.MattermostSentEntity;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.HashMap;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class MattermostReader {
    //    public static final String FIND_ALL_MATTERMOST_SENT_FIX_PAGE_0 = "findAllMattermostSentFixPage0";
    private static final int PAGE_SIZE = 100;
    public static final String FIND_BY_CATEGORY_IS_NEWS = "findByCategoryIsNews";
    public static final String FIND_BY_CATEGORY_IS_COIN = "findByCategoryIsCoin";
    public static final String FIND_BY_CATEGORY_IS_HOTDEAL = "findByCategoryIsHotdeal";

    @Bean(name = FIND_BY_CATEGORY_IS_NEWS, destroyMethod = "")
    @StepScope
    public JpaPagingItemReader<MattermostSentEntity> findByCategoryIsNews(@Qualifier("crawlingEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        JpaPagingItemReader<MattermostSentEntity> reader = new DelJpaPagingItemReader<>();

        reader.setName("jpaPagingItemReader");
        reader.setPageSize(PAGE_SIZE);
        reader.setEntityManagerFactory(entityManagerFactory);
        reader.setQueryString("SELECT e FROM MattermostSentEntity e WHERE e.createDate < :date and e.category = 'news' order by e.createDate");

        HashMap<String, Object> param = new HashMap<>();
        param.put("date", LocalDateTime.now().minusHours(24));
        reader.setParameterValues(param);
        return reader;
    }

    @Bean(name = FIND_BY_CATEGORY_IS_COIN, destroyMethod = "")
    @StepScope
    public JpaPagingItemReader<MattermostSentEntity> findByCategoryIsCoin(@Qualifier("crawlingEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        JpaPagingItemReader<MattermostSentEntity> reader = new DelJpaPagingItemReader<>();

        reader.setName("jpaPagingItemReader");
        reader.setPageSize(PAGE_SIZE);
        reader.setEntityManagerFactory(entityManagerFactory);
        reader.setQueryString("SELECT e FROM MattermostSentEntity e WHERE e.createDate < :date and e.category = 'coin'");

        HashMap<String, Object> param = new HashMap<>();
        param.put("date", LocalDateTime.now().minusHours(72));
        reader.setParameterValues(param);
        return reader;
    }

    @Bean(name = FIND_BY_CATEGORY_IS_HOTDEAL, destroyMethod = "")
    @StepScope
    public JpaPagingItemReader<MattermostSentEntity> findByCategoryIsHotdeal(@Qualifier("crawlingEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        JpaPagingItemReader<MattermostSentEntity> reader = new DelJpaPagingItemReader<>();

        reader.setName("jpaPagingItemReader");
        reader.setPageSize(PAGE_SIZE);
        reader.setEntityManagerFactory(entityManagerFactory);
        reader.setQueryString("SELECT e FROM MattermostSentEntity e WHERE e.createDate < :date and e.category = 'hotdeal' order by e.createDate");

        HashMap<String, Object> param = new HashMap<>();
        param.put("date", LocalDateTime.now().minusDays(1));
        reader.setParameterValues(param);
        return reader;
    }
}
