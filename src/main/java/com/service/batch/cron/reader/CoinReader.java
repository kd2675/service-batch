package com.service.batch.cron.reader;

import com.service.batch.cron.common.DelJpaPagingItemReader;
import com.service.batch.cron.step.CoinStep;
import com.service.batch.database.crawling.entity.CoinEntity;
import com.service.batch.database.crawling.repository.CoinREP;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.HashMap;

@RequiredArgsConstructor
@Configuration
public class CoinReader {
    private static final int BEFORE_MONTH_RANGE = 1;
    public static final String FIND_COIN_ENTITY_BEFORE_CREATE_DATE = "findCoinEntityBeforeCreateDate";
    public static final String FIND_TOP_10_BY_ORDER_BY_ID_DESC = "findTop10ByOrderByIdDesc";
    public static final String FIND_TOP_1_BY_ORDER_BY_ID_DESC = "findTop1ByOrderByIdDesc";

    private final CoinREP coinREP;

    @Bean(name = FIND_COIN_ENTITY_BEFORE_CREATE_DATE, destroyMethod = "")
    @StepScope
    public JpaPagingItemReader<CoinEntity> jpaPagingItemReader(@Qualifier("crawlingEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        JpaPagingItemReader<CoinEntity> reader = new DelJpaPagingItemReader<>();

        reader.setName("jpaPagingItemReader");
        reader.setPageSize(CoinStep.PAGE_SIZE);
        reader.setEntityManagerFactory(entityManagerFactory);
        reader.setQueryString("SELECT p FROM CoinEntity p WHERE p.createDate <= :date");

        HashMap<String, Object> param = new HashMap<>();
        param.put("date", LocalDateTime.now().minusMonths(BEFORE_MONTH_RANGE));
        reader.setParameterValues(param);

        return reader;
    }

    @Bean(name = FIND_TOP_10_BY_ORDER_BY_ID_DESC, destroyMethod = "")
    @StepScope
    public ListItemReader<CoinEntity> findTop10ByOrderByIdDesc(@Qualifier("crawlingEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new ListItemReader<>(coinREP.findTop10ByOrderByIdDesc());
    }
    @Bean(name = FIND_TOP_1_BY_ORDER_BY_ID_DESC, destroyMethod = "")
    @StepScope
    public ListItemReader<CoinEntity> findTop1ByOrderByIdDesc(@Qualifier("crawlingEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new ListItemReader<>(coinREP.findTop1ByOrderByIdDesc());
    }
}
