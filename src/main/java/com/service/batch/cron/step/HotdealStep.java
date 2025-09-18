package com.service.batch.cron.step;

import com.service.batch.cron.common.BasicProcessor;
import com.service.batch.cron.common.BasicWriter;
import com.service.batch.cron.processor.HotdealProcessor;
import com.service.batch.cron.reader.HotdealReader;
import com.service.batch.cron.reader.MattermostReader;
import com.service.batch.cron.writer.HotdealComposeWriter;
import com.service.batch.cron.writer.HotdealWriter;
import com.service.batch.cron.writer.MattermostComposeWriter;
import com.service.batch.database.crawling.dto.HotdealDTO;
import com.service.batch.database.crawling.entity.HotdealEntity;
import com.service.batch.database.crawling.entity.MattermostSentEntity;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class HotdealStep {
    public static final int CHUNK_SIZE = 100;
    public static final int PAGE_SIZE = 100;
    public static final String INS_HOTDEAL_STEP = "insHotdealStep";
    public static final String SEND_HOTDEAL_STEP = "sendHotdealStep";
    public static final String DEL_SENT_HOTDEAL_STEP = "delSentHotdealStep";
    public static final String DEL_HOTDEAL_STEP = "delHotdealStep";

    @Bean(name = INS_HOTDEAL_STEP)
    @JobScope
    public Step insHotdealStep(
            JobRepository jobRepository,
            @Qualifier("crawlingTransactionManager") PlatformTransactionManager platformTransactionManager,
            @Qualifier(HotdealReader.FIND_HOTDEAL) ListItemReader<HotdealDTO> itemReader,
            @Qualifier(HotdealProcessor.INS_HOTDEAL_PROCESSOR) BasicProcessor<HotdealDTO, HotdealEntity> itemProcessor,
            @Qualifier(HotdealWriter.JPA_ITEM_WRITER) JpaItemWriter<HotdealEntity> itemWriter
    ) {
        return new StepBuilder(INS_HOTDEAL_STEP, jobRepository)
                .<HotdealDTO, HotdealEntity>chunk(CHUNK_SIZE, platformTransactionManager)
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
//                .allowStartIfComplete(true)
                .build();
    }

    @Bean(name = SEND_HOTDEAL_STEP)
    @JobScope
    public Step sendHotdealStep(
            JobRepository jobRepository,
            @Qualifier("crawlingTransactionManager") PlatformTransactionManager platformTransactionManager,
            @Qualifier(HotdealReader.FIND_ALL_HOTDEAL_SEND_YN_N) ListItemReader<HotdealEntity> itemReader,
            @Qualifier(HotdealProcessor.UPD_HOTDEAL_SEND_YN_Y) BasicProcessor<HotdealEntity, HotdealEntity> itemProcessor,
            @Qualifier(HotdealComposeWriter.HOTDEAL_MATTERMOST_SEND_AND_UPD_SEND_YN) CompositeItemWriter<HotdealEntity> itemWriter
    ) {
        return new StepBuilder(SEND_HOTDEAL_STEP, jobRepository)
                .<HotdealEntity, HotdealEntity>chunk(15, platformTransactionManager)
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
//                .allowStartIfComplete(true)
                .build();
    }

    @Bean(name = DEL_SENT_HOTDEAL_STEP)
    @JobScope
    public Step delSentHotdealStep(
            JobRepository jobRepository,
            @Qualifier("crawlingTransactionManager") PlatformTransactionManager platformTransactionManager,
            @Qualifier(MattermostReader.FIND_BY_CATEGORY_IS_HOTDEAL) JpaPagingItemReader<MattermostSentEntity> itemReader,
            @Qualifier(MattermostComposeWriter.DEL_MATTERMOST_UTIL_BY_ID_AND_DEL_ALL_MATTERMOST_SENT) CompositeItemWriter<MattermostSentEntity> itemCompose
    ) {
        return new StepBuilder(DEL_SENT_HOTDEAL_STEP, jobRepository)
                .<MattermostSentEntity, MattermostSentEntity>chunk(CHUNK_SIZE, platformTransactionManager)
                .reader(itemReader)
                .writer(itemCompose)
//                .allowStartIfComplete(true)
//                .faultTolerant()
//                .skip(HttpClientErrorException.class).skipLimit(10)
                .build();
    }

    @Bean(name = DEL_HOTDEAL_STEP)
    @JobScope
    public Step delHotdealStep(
            JobRepository jobRepository,
            @Qualifier("crawlingTransactionManager") PlatformTransactionManager platformTransactionManager,
            @Qualifier(HotdealReader.FIND_ALL_HOTDEAL_FIX_PAGE_0) JpaPagingItemReader<HotdealEntity> itemReader,
            @Qualifier(HotdealWriter.DEL_ALL_HOTDEAL) BasicWriter<HotdealEntity> itemWriter
    ) {
        return new StepBuilder(DEL_HOTDEAL_STEP, jobRepository)
//                .allowStartIfComplete(true)
                .<HotdealEntity, HotdealEntity>chunk(CHUNK_SIZE, platformTransactionManager)
                .reader(itemReader)
                .writer(itemWriter)
                .build();
    }
}
