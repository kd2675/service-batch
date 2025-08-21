package com.service.batch.cron.processor;

import com.service.batch.cron.common.BasicProcessor;
import com.service.batch.database.crawling.entity.CoinEntity;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CoinProcessor {
    public static final String GET_ID_COIN_ENTITY = "getIdCoinEntity";
    @Bean(name = GET_ID_COIN_ENTITY)
    @StepScope
    public BasicProcessor<CoinEntity, Long> itemProcessor() {
        return new BasicProcessor<CoinEntity, Long>() {
            @Override
            public Long process(CoinEntity item) throws Exception {
                return item.getId();
            }
        };
//        return CoinEntity::getId;
//        return item -> CoinDTO.ofEntity(item).getId();
//        return item -> item;
    }
}
