package com.service.batch.cron.job;

import com.service.batch.cron.step.CoinStep;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CoinJob {
    public static final String DEL_COIN_JOB = "delCoinJob";
    public static final String SEND_COIN_JOB = "sendCoinJob";

    @Bean(name = SEND_COIN_JOB)
    public Job sendCoinJob(
            JobRepository jobRepository,
            @Qualifier(CoinStep.SEND_COIN_STEP) Step step
    ) {
        return new JobBuilder(SEND_COIN_JOB, jobRepository)
                .start(step)
                .build();
    }

    @Bean(name = DEL_COIN_JOB)
    public Job delCoinJob(
            JobRepository jobRepository,
            @Qualifier(CoinStep.SENT_COIN_STEP) Step step,
            @Qualifier(CoinStep.DEL_COIN_STEP) Step coinDeleteStep
    ) {
        return new JobBuilder(DEL_COIN_JOB, jobRepository)
                .start(step)
                .next(coinDeleteStep)
                .build();
    }
}
