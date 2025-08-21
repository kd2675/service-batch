package com.service.batch.cron.job;

import com.service.batch.cron.step.NewsStep;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NewsJob {
    public static final String INS_NEWS_JOB = "insNewsJob";
    public static final String SEND_NEWS_JOB = "sendNewsJob";
    public static final String SEND_NEWS_FLASH_JOB = "sendNewsFlashJob";
    public static final String SEND_NEWS_MARKETING_JOB = "sendNewsMarketingJob";
    public static final String SEND_NEWS_STOCK_JOB = "sendNewsStockJob";
    public static final String DEL_NEWS_JOB = "delNewsJob";

    @Bean(name = INS_NEWS_JOB)
    public Job insNewsJob(
            JobRepository jobRepository,
            @Qualifier(NewsStep.INS_NEWS_STEP) Step step
    ) {
        return new JobBuilder(INS_NEWS_JOB, jobRepository)
                .start(step)
                .build();
    }

    @Bean(name = SEND_NEWS_JOB)
    public Job sendNewsJob(
            JobRepository jobRepository,
            @Qualifier(NewsStep.SEND_NEWS_STEP) Step step
    ) {
        return new JobBuilder(SEND_NEWS_JOB, jobRepository)
                .start(step)
                .build();
    }

    @Bean(name = SEND_NEWS_FLASH_JOB)
    public Job sendNewsFlashJob(
            JobRepository jobRepository,
            @Qualifier(NewsStep.SEND_NEWS_FLASH_STEP) Step step
    ) {
        return new JobBuilder(SEND_NEWS_FLASH_JOB, jobRepository)
                .start(step)
                .build();
    }

    @Bean(name = SEND_NEWS_MARKETING_JOB)
    public Job sendNewsMarketingJob(
            JobRepository jobRepository,
            @Qualifier(NewsStep.SEND_NEWS_MARKETING_STEP) Step step
    ) {
        return new JobBuilder(SEND_NEWS_MARKETING_JOB, jobRepository)
                .start(step)
                .build();
    }

    @Bean(name = SEND_NEWS_STOCK_JOB)
    public Job sendNewsStockJob(
            JobRepository jobRepository,
            @Qualifier(NewsStep.SEND_NEWS_STOCK_STEP) Step step
    ) {
        return new JobBuilder(SEND_NEWS_STOCK_JOB, jobRepository)
                .start(step)
                .build();
    }

    @Bean(name = DEL_NEWS_JOB)
    public Job delNewsJob(
            JobRepository jobRepository,
            @Qualifier(NewsStep.SENT_NEWS_STEP) Step step0,
            @Qualifier(NewsStep.SAVE_OLD_NEWS_AND_DEL_ALL_NEWS_STEP) Step step1
    ) {
        return new JobBuilder(DEL_NEWS_JOB, jobRepository)
                .start(step0)
                .next(step1)
                .build();
    }
}
