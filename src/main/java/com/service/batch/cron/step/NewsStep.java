package com.service.batch.cron.step;

import com.service.batch.cron.common.BasicProcessor;
import com.service.batch.cron.processor.NewsProcessor;
import com.service.batch.cron.reader.MattermostReader;
import com.service.batch.cron.reader.NewsReader;
import com.service.batch.cron.writer.MattermostComposeWriter;
import com.service.batch.cron.writer.NewsComposeWriter;
import com.service.batch.cron.writer.NewsWriter;
import com.service.batch.database.crawling.entity.MattermostSentEntity;
import com.service.batch.database.crawling.entity.NewsEntity;
import com.service.batch.service.news.api.vo.NaverNewsApiItemVO;
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
public class NewsStep {
    private static final int CHUNK_SIZE = 100;

    public static final String INS_NEWS_STEP = "insNewsStep";
    public static final String SEND_NEWS_STEP = "sendNewsStep";
    public static final String SEND_NEWS_FLASH_STEP = "sendNewsFlashStep";
    public static final String SEND_NEWS_MARKETING_STEP = "sendNewsMarketingStep";
    public static final String SEND_NEWS_STOCK_STEP = "sendNewsStockStep";
    public static final String SAVE_OLD_NEWS_AND_DEL_ALL_NEWS_STEP = "saveOldNewsAndDelAllNewsStep";
    public static final String SENT_NEWS_STEP = "sentNewsStep";

    @Bean(name = INS_NEWS_STEP)
    @JobScope
    public Step insNewsStep(
            JobRepository jobRepository,
            @Qualifier("crawlingTransactionManager") PlatformTransactionManager platformTransactionManager,
            @Qualifier(NewsReader.FIND_NAVER_NEWS_API) ListItemReader<NaverNewsApiItemVO> itemReader,
            @Qualifier(NewsProcessor.NAVER_NEWS_API_ITEM_VO_TO_NEWS_ENTITY) BasicProcessor<NaverNewsApiItemVO, NewsEntity> itemProcessor,
            @Qualifier(NewsWriter.JPA_ITEM_WRITER) JpaItemWriter<NewsEntity> itemWriter
    ) {
        return new StepBuilder(INS_NEWS_STEP, jobRepository)
                .<NaverNewsApiItemVO, NewsEntity>chunk(CHUNK_SIZE, platformTransactionManager)
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
//                .allowStartIfComplete(true)
                .build();
    }

    @Bean(name = SEND_NEWS_STEP)
    @JobScope
    public Step sendNewsStep(
            JobRepository jobRepository,
            @Qualifier("crawlingTransactionManager") PlatformTransactionManager platformTransactionManager,
            @Qualifier(NewsReader.FIND_TOP_15_NEWS) ListItemReader<NewsEntity> itemReader,
            @Qualifier(NewsProcessor.NEWS_ENTITY_UPD_SEND_YN_Y) BasicProcessor<NewsEntity, NewsEntity> itemProcessor,
            @Qualifier(NewsComposeWriter.SEND_NEWS_TO_MATTERMOST_AND_SAVE_MATTERMOST_SENT_AND_UPD_SEND_YN) CompositeItemWriter<NewsEntity> itemWriter
    ) {
        return new StepBuilder(SEND_NEWS_STEP, jobRepository)
                .<NewsEntity, NewsEntity>chunk(15, platformTransactionManager)
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
//                .allowStartIfComplete(true)
                .build();
    }

    @Bean(name = SEND_NEWS_FLASH_STEP)
    @JobScope
    public Step sendNewsFlashStep(
            JobRepository jobRepository,
            @Qualifier("crawlingTransactionManager") PlatformTransactionManager platformTransactionManager,
            @Qualifier(NewsReader.FIND_TOP_15_NEWS_FLASH) ListItemReader<NewsEntity> itemReader,
            @Qualifier(NewsProcessor.NEWS_ENTITY_UPD_SEND_YN_Y) BasicProcessor<NewsEntity, NewsEntity> itemProcessor,
            @Qualifier(NewsComposeWriter.SEND_NEWS_FLASH_TO_MATTERMOST_AND_SAVE_MATTERMOST_SENT_AND_UPD_SEND_YN) CompositeItemWriter<NewsEntity> itemWriter
    ) {
        return new StepBuilder(SEND_NEWS_FLASH_STEP, jobRepository)
                .<NewsEntity, NewsEntity>chunk(15, platformTransactionManager)
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
//                .allowStartIfComplete(true)
                .build();
    }

    @Bean(name = SEND_NEWS_MARKETING_STEP)
    @JobScope
    public Step sendNewsMarketingStep(
            JobRepository jobRepository,
            @Qualifier("crawlingTransactionManager") PlatformTransactionManager platformTransactionManager,
            @Qualifier(NewsReader.FIND_TOP_15_NEWS_MARKETING) ListItemReader<NewsEntity> itemReader,
            @Qualifier(NewsProcessor.NEWS_ENTITY_UPD_SEND_YN_Y) BasicProcessor<NewsEntity, NewsEntity> itemProcessor,
            @Qualifier(NewsComposeWriter.SEND_NEWS_MARKETING_TO_MATTERMOST_AND_SAVE_MATTERMOST_SENT_AND_UPD_SEND_YN) CompositeItemWriter<NewsEntity> itemWriter
    ) {
        return new StepBuilder(SEND_NEWS_MARKETING_STEP, jobRepository)
                .<NewsEntity, NewsEntity>chunk(15, platformTransactionManager)
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
//                .allowStartIfComplete(true)
                .build();
    }

    @Bean(name = SEND_NEWS_STOCK_STEP)
    @JobScope
    public Step sendNewsStockStep(
            JobRepository jobRepository,
            @Qualifier("crawlingTransactionManager") PlatformTransactionManager platformTransactionManager,
            @Qualifier(NewsReader.FIND_TOP_15_NEWS_STOCK) ListItemReader<NewsEntity> itemReader,
            @Qualifier(NewsProcessor.NEWS_ENTITY_UPD_SEND_YN_Y) BasicProcessor<NewsEntity, NewsEntity> itemProcessor,
            @Qualifier(NewsComposeWriter.SEND_NEWS_STOCK_TO_MATTERMOST_AND_SAVE_MATTERMOST_SENT_AND_UPD_SEND_YN) CompositeItemWriter<NewsEntity> itemWriter
    ) {
        return new StepBuilder(SEND_NEWS_STOCK_STEP, jobRepository)
                .<NewsEntity, NewsEntity>chunk(15, platformTransactionManager)
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
//                .allowStartIfComplete(true)
                .build();
    }

    @Bean(name = SENT_NEWS_STEP)
    @JobScope
    public Step sentNewsStep(
            JobRepository jobRepository,
            @Qualifier("crawlingTransactionManager") PlatformTransactionManager platformTransactionManager,
            @Qualifier(MattermostReader.FIND_BY_CATEGORY_IS_NEWS) JpaPagingItemReader<MattermostSentEntity> itemReader,
            @Qualifier(MattermostComposeWriter.DEL_MATTERMOST_UTIL_BY_ID_AND_DEL_ALL_MATTERMOST_SENT) CompositeItemWriter<MattermostSentEntity> itemCompose
    ) {
        return new StepBuilder(SENT_NEWS_STEP, jobRepository)
                .<MattermostSentEntity, MattermostSentEntity>chunk(CHUNK_SIZE, platformTransactionManager)
                .reader(itemReader)
                .writer(itemCompose)
//                .allowStartIfComplete(true)
//                .faultTolerant()
//                .skip(HttpClientErrorException.class).skipLimit(10)
                .build();
    }

    @Bean(name = SAVE_OLD_NEWS_AND_DEL_ALL_NEWS_STEP)
    @JobScope
    public Step saveOldNewsAndDelAllNewsStep(
            JobRepository jobRepository,
            @Qualifier("crawlingTransactionManager") PlatformTransactionManager platformTransactionManager,
            @Qualifier(NewsReader.FIND_ALL_NEWS_FIX_PAGE_0) JpaPagingItemReader<NewsEntity> itemReader,
            @Qualifier(NewsComposeWriter.SAVE_OLD_NEWS_AND_DEL_ALL_NEWS) CompositeItemWriter<NewsEntity> itemCompose
    ) {
        return new StepBuilder(SAVE_OLD_NEWS_AND_DEL_ALL_NEWS_STEP, jobRepository)
//                .allowStartIfComplete(true)
                .<NewsEntity, NewsEntity>chunk(CHUNK_SIZE, platformTransactionManager)
                .reader(itemReader)
                .writer(itemCompose)
                .build();
    }
}
