package com.service.batch.cron.step;

import com.service.batch.cron.common.BasicProcessor;
import com.service.batch.cron.processor.OrderProcessor;
import com.service.batch.cron.reader.OrderReader;
import com.service.batch.cron.writer.OrderComposeWriter;
import com.service.batch.database.pub.entity.OrderEntity;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class OrderStep {
    public static final int CHUNK_SIZE = 1;

    public static final String UPD_ORDER_STEP = "updOrderStep";

    @Bean(name = UPD_ORDER_STEP)
    @JobScope
    public Step adjOrderStep(
            JobRepository jobRepository,
            @Qualifier("pubTransactionManager") PlatformTransactionManager platformTransactionManager,
            @Qualifier(OrderReader.FIND_ORDER_ENTITY) JpaPagingItemReader<OrderEntity> itemReader,
            @Qualifier(OrderProcessor.ADJ_ORDER_PROCESSOR) BasicProcessor<OrderEntity, OrderEntity> itemProcessor,
            @Qualifier(OrderComposeWriter.COMPLETE_ORDER_TO_MARKET_AND_DEL_ORDER) CompositeItemWriter<OrderEntity> itemWriter
    ) {
        return new StepBuilder(UPD_ORDER_STEP, jobRepository)
                .<OrderEntity, OrderEntity>chunk(CHUNK_SIZE, platformTransactionManager)
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
//                .allowStartIfComplete(true)
                .build();
    }
}
