package com.service.batch.cron.step;

import com.service.batch.cron.common.BasicProcessor;
import com.service.batch.cron.common.BasicWriter;
import com.service.batch.cron.processor.CoinProcessor;
import com.service.batch.cron.reader.CoinReader;
import com.service.batch.cron.reader.MattermostReader;
import com.service.batch.cron.writer.CoinWriter;
import com.service.batch.cron.writer.MattermostComposeWriter;
import com.service.batch.cron.writer.MattermostWriter;
import com.service.batch.database.crawling.entity.CoinEntity;
import com.service.batch.database.crawling.entity.MattermostSentEntity;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class CoinStep {
    public static final int CHUNK_SIZE = 100;
    public static final int PAGE_SIZE = 100;
    public static final String DEL_COIN_STEP = "delCoinStep";
    public static final String SEND_COIN_STEP = "sendCoinStep";
    public static final String SENT_COIN_STEP = "sentCoinStep";

    @Bean(name = SEND_COIN_STEP)
    @JobScope
    public Step sendCoinStep(
            JobRepository jobRepository,
            @Qualifier("crawlingTransactionManager") PlatformTransactionManager platformTransactionManager,
            @Qualifier(CoinReader.FIND_TOP_1_BY_ORDER_BY_ID_DESC) ListItemReader<CoinEntity> itemReader,
            @Qualifier(MattermostWriter.SEND_COIN_AND_SAVE_MATTERMOST_SENT) BasicWriter<CoinEntity> itemWriter
    ) {
        return new StepBuilder(SEND_COIN_STEP, jobRepository)
                .<CoinEntity, CoinEntity>chunk(10, platformTransactionManager)
                .reader(itemReader)
                .writer(itemWriter)
//                .allowStartIfComplete(true)
                .build();
    }

    @Bean(name = DEL_COIN_STEP)
    @JobScope
    public Step coinDeleteStep(
            JobRepository jobRepository,
            @Qualifier("crawlingTransactionManager") PlatformTransactionManager platformTransactionManager,
            @Qualifier(CoinReader.FIND_COIN_ENTITY_BEFORE_CREATE_DATE) JpaPagingItemReader<CoinEntity> itemReader,
            @Qualifier(CoinProcessor.GET_ID_COIN_ENTITY) BasicProcessor<CoinEntity, Long> itemProcessor,
            @Qualifier(CoinWriter.DEL_COIN_BY_ID) BasicWriter<Long> itemWriter
    ) {
        return new StepBuilder(DEL_COIN_STEP, jobRepository)
                .<CoinEntity, Long>chunk(CHUNK_SIZE, platformTransactionManager)
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
//                .allowStartIfComplete(true)
                .build();
    }

    @Bean(name = SENT_COIN_STEP)
    @JobScope
    public Step sentCoinStep(
            JobRepository jobRepository,
            @Qualifier("crawlingTransactionManager") PlatformTransactionManager platformTransactionManager,
            @Qualifier(MattermostReader.FIND_BY_CATEGORY_IS_COIN) JpaPagingItemReader<MattermostSentEntity> itemReader,
            @Qualifier(MattermostComposeWriter.DEL_MATTERMOST_UTIL_BY_ID_AND_DEL_ALL_MATTERMOST_SENT) CompositeItemWriter<MattermostSentEntity> itemCompose
    ) {
        return new StepBuilder(SENT_COIN_STEP, jobRepository)
                .<MattermostSentEntity, MattermostSentEntity>chunk(CHUNK_SIZE, platformTransactionManager)
                .reader(itemReader)
                .writer(itemCompose)
//                .allowStartIfComplete(true)
//                .faultTolerant()
//                .skip(HttpClientErrorException.class).skipLimit(10)
                .build();
    }
}
